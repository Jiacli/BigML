# Matrix Factorization Using Spark

import sys
import numpy as np
from pyspark import SparkConf, SparkContext


# parse the raw input of the data
def parse_line(record):
    uid, mid, rating = record.split(',')
    return int(uid), int(mid), int(rating)
    
# indexing the data matrix V
def data_indexing(record, broadcastVar, row_counts, col_counts):
    (uid, mid, rating) = record
    num_workers, mov_bound, usr_bound = broadcastVar.value
    i = -1
    j = -1
    for idx in xrange(num_workers):
        (lower, upper) = mov_bound[idx]
        if mid >= lower and mid <= upper:
            j = idx
            break
    for idx in xrange(num_workers):
        (lower, upper) = usr_bound[idx]
        if uid >= lower and uid <= upper:
            i = idx
            break
    if j - i < 0:
        i = j + num_workers - i
    else:
        i = j - i
    N_i = 0
    N_j = 0
    if uid in row_counts:
        N_i = row_counts[uid]
    if mid in col_counts:
        N_j = col_counts[mid]
    return (i, j, uid, mid, rating, N_i, N_j)


def compute_boundary(max_mid, max_uid, num_workers):
    # assume index from 0
    mov_step = int((max_mid+1) / num_workers)
    usr_step = int((max_uid+1) / num_workers)

    mov_interval = []
    usr_interval = []

    for idx in xrange(num_workers):
        if idx != num_workers-1:
            mov_interval.append((idx*mov_step, (idx+1)*mov_step-1))
            usr_interval.append((idx*usr_step, (idx+1)*usr_step-1))
        else:
            mov_interval.append((idx*mov_step, max_mid))
            usr_interval.append((idx*usr_step, max_uid))
    return mov_interval, usr_interval

def main(args):
    # load parameters
    num_factors = int(args[1])
    num_workers = int(args[2])
    num_iter = int(args[3])
    beta_v = float(args[4])
    lambda_v = float(args[5])
    inputV = args[6]
    outputW = args[7]
    outputH = args[8]

    # setting up spark
    #conf = SparkConf().setAppName('DSGD_MF')
    sc = SparkContext(appName='DSGD_MF')

    # load file, data format:
    # <user_id>,<movid_id>,<rating>
    lines = sc.textFile(inputV)
    V = lines.map(lambda line: parse_line(line)).cache()
    # find the max value for user_id and movie_id
    max_uid, max_mid = V.reduce(lambda x, y: (max(x[0], y[0]), max(x[1], y[1])))
    print 'max_mid=', max_mid, 'max_uid=', max_uid

    # get the N_i* and N_*j
    row_counts = V.map(lambda x: (x[0], 1)).countByKey()
    col_counts = V.map(lambda x: (x[1], 1)).countByKey()

    # partition using max ids
    # compute boundary
    mov_bound, usr_bound = compute_boundary(max_mid, max_uid, num_workers)
    print 'mov_bound=',mov_bound, '\nusr_bound=', usr_bound
    broadcastVar = sc.broadcast([num_workers, mov_bound, usr_bound])

    # index the V matrix
    Vidx = V.map(lambda record: data_indexing(record, broadcastVar, row_counts, col_counts)).cache()
    #V.unpersist()

    # create the strata
    strata_list = []
    strata_count = []
    for i in xrange(num_workers):
        strata = Vidx.filter(lambda x: x[0]==i).map(lambda x: (x[1], (x[2], x[3], x[4], x[5], x[6]))) \
                    .partitionBy(num_workers).cache()
        count = strata.count()
        strata_list.append(strata)
        strata_count.append(count)
    Vidx.unpersist()

    # validate partitions
    #l = strata_list[0].glom().collect()
    #for part in l:
    #    print 'partition=', part[1:10]
    #print strata_count

    # build local parameters
    W = np.random.rand(max_uid + 1, num_factors)
    H = np.random.rand(max_mid + 1, num_factors)
    params = sc.broadcast([beta_v, lambda_v])
    MSE = []

    sumMSE, n = V.map(lambda x: evaluation(x, W, H)).reduce(lambda x,y: (x[0]+y[0], x[1]+y[1]))
    MSE.append(sumMSE / n)

    # perform gradient
    m = 0
    for t in xrange(num_iter):
        strata = strata_list[t % num_workers]
        results = strata.mapPartitions(lambda x: updateWeights(x, W, H, t, m, params)).collect()

        # update weights
        for x in results:
            if x[0] == 'W':
                W[x[1]] = x[2]
            elif x[0] == 'H':
                H[x[1]] = x[2]

        # evaluation
        sumMSE, n = V.map(lambda x: evaluation(x, W, H)).reduce(lambda x,y: (x[0]+y[0], x[1]+y[1]))
        MSE.append(sumMSE / n)


        # update the # of iteration
        m += strata_count[t % num_workers]

    print MSE

def evaluation(record, W, H):
    (uid, mid, rating) = record
    err = (rating - np.dot(W[uid], H[mid]))**2
    return (err, 1)


def updateWeights(iterator, W, H, t, m, params):
    beta_v, lambda_v = params.value
    epsilon = (100 + t + m)**(-beta_v)
    W_set = set()
    H_set = set()

    for record in iterator:
        # uid, mid, rating, N_i, N_j
        (i, j, v, N_i, N_j) = record[1]
        tmp = -2 * (v - np.dot(W[i], H[j]))
        grad_w = tmp * H[j] + 2 * lambda_v / N_i * W[i]
        grad_h = tmp * W[i] + 2 * lambda_v / N_j * H[j]

        W[i] -= epsilon * grad_w
        H[j] -= epsilon * grad_h
        W_set.add(i)
        H_set.add(j)
    results = []

    for i in W_set:
        results.append(('W', i, W[i]))
    for j in H_set:
        results.append(('H', j, H[j]))
    W_set.clear()
    H_set.clear()

    return results

def calcGradientx(iterator, W, H):
    #records = list(iterator)
    maxuid = -1
    maxmid = -1
    for record in iterator:
        if record[1][0] > maxuid:
            maxuid = record[1][0]
        if record[1][1] > maxmid:
            maxmid = record[1][1]
    return [(maxuid, maxmid),(maxuid, maxmid)]

if __name__ == '__main__':
    # usage
    if len(sys.argv) != 9:
        print 'Usage: spark-submit dsgd_mf.py <num_factors> <num_workers> \
                <num_iterations> <beta_value> <lambda_value> \
                <inputV_filepath> <outputW_filepath> <outputH_filepath>'
        #sys.exit(-1)
    

    main(sys.argv)