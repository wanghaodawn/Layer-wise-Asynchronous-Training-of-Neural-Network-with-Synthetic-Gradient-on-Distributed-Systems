# Use these interfaces to use RESTful API provided by master
# 
# These interfaces have adjusted the level as needed, 
# just need to use current level to use these interfaces

import urllib2

# TODO: Every time need to update the master_url
master_url = "http://127.0.0.1:8000"


def insert_true_input(level, iteration, true_input):
    url = master_url + "/insert_true_input?level=" + str(level) + "&iteration=" + str(iteration)
    url += "&true_input=" + true_input
    res = urllib2.urlopen(url).read()
    return res


def insert_true_gradient(level, iteration, true_gradient):
    url = master_url + "/insert_true_gradient?level=" + str(level) + "&iteration=" + str(iteration)
    url += "&true_gradient=" + true_gradient
    res = urllib2.urlopen(url).read()
    return res


def get_true_input(level, iteration):
    level += 1
    url = master_url + "/get_true_input?level=" + str(level) + "&iteration=" + str(iteration)
    res = urllib2.urlopen(url).read()
    return res


def get_true_gradient(level, iteration):
    level -= 1
    url = master_url + "/get_true_gradient?level=" + str(level) + "&iteration=" + str(iteration)
    res = urllib2.urlopen(url).read()
    return res