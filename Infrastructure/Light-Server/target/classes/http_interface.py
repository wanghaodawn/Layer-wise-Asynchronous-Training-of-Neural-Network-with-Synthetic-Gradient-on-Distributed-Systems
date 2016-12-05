# Use these interfaces to use RESTful API provided by master
# 
# These interfaces have adjusted the level as needed, 
# just need to use current level to use these interfaces

import requests

# TODO: Every time need to update the master_url
master_url = "http://127.0.0.1:8000"


def remove_simbols(text):
    return text.\
            replace('\a', '\\a').\
            replace('\b', '\\b').\
            replace('\f', '\\f').\
            replace('\n', '\\n').\
            replace('\r', '\\r').\
            replace('\t', '\\t').\
            replace('\v', '\\v').\
            replace('\"', '\\"')


def recover_simbols(text):
    return text.\
            replace('\\"', '\"').\
            replace('\\v', '\v').\
            replace('\\t', '\t').\
            replace('\\r', '\r').\
            replace('\\n', '\n').\
            replace('\\f', '\f').\
            replace('\\b', '\b').\
            replace('\\a', '\a')


def insert_true_input(level, iteration, true_input):
    url = master_url + "/insert_true_input?"
    data = {'level': level, 'iteration': iteration, 'true_input': remove_simbols(true_input)}
    res = requests.post(url, data)
    return res.text


def insert_true_gradient(level, iteration, true_gradient):
    url = master_url + "/insert_true_gradient?level=" + str(level) + "&iteration=" + str(iteration)
    url += "&true_gradient=" + remove_simbols(true_gradient)
    res = urllib2.urlopen(url).read()
    return res


def get_true_input(level):
    level += 1
    url = master_url + "/get_true_input?level=" + str(level)
    res = recover_simbols(urllib2.urlopen(url).read())
    return res


def get_true_gradient(level):
    level -= 1
    url = master_url + "/get_true_gradient?level=" + str(level)
    res = recover_simbols(urllib2.urlopen(url).read())
    return res

