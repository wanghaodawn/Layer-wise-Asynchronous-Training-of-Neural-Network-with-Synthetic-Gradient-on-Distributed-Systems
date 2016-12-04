import urllib2


def insert_true_input(level, iteration, true_input, master_url):
    url = master_url + "/insert_true_input?level=" + str(level) + "&iteration=" + str(iteration)
    url += "&true_input=" + true_input
    res = urllib2.urlopen(url).read()
    return res


def insert_true_gradient(level, iteration, true_gradient, master_url):
    url = master_url + "/insert_true_gradient?level=" + str(level) + "&iteration=" + str(iteration)
    url += "&true_gradient=" + true_gradient
    res = urllib2.urlopen(url).read()
    return res


def get_true_input(level, iteration, master_url):
    level += 1
    url = master_url + "/get_true_input?level=" + str(level) + "&iteration=" + str(iteration)
    res = urllib2.urlopen(url).read()
    return res


def get_true_gradient(level, iteration, master_url):
    level -= 1
    url = master_url + "/get_true_gradient?level=" + str(level) + "&iteration=" + str(iteration)
    res = urllib2.urlopen(url).read()
    return res