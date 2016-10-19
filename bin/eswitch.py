#! /usr/bin/python
# encoding: utf-8

import urllib2, urllib
from optparse import OptionParser

PORT = '30000'
RES  = 'http://%s:%s/eswitch/config'

def access(host, port=PORT, action=None, data={}):
    url = RES % (host ,port)
    data['action'] = action
    r = urllib2.urlopen(url, urllib.urlencode(data))
    print r.read()


parser = OptionParser(usage='%prog [-options] <on/off> <threshold>')
parser.add_option('-l', '--host',   dest='host',   help='specify eswitch host')
parser.add_option('-p', '--port',   dest='port',   default=PORT,  help='specify eswitch port')
parser.add_option('-i', '--item',   dest='item',   help='specify eswitch item\' name')
parser.add_option('-c', '--action', dest='action', help='specify eswitch action')
opts, args = parser.parse_args()

if 'print' == opts.action:
    access(opts.host, opts.port, opts.action)
elif 'modify' == opts.action:
    param = {'item' : opts.item};
    for i in opts.item.split(","):
       param[i] = {'on' : args[0], 'threshold' : args[1]};
    access(opts.host, opts.port, opts.action, param)
elif 'reload' == opts.action:
    access(opts.host, opts.port, opts.action)
else:
    parser.print_help()
