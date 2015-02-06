#!/usr/bin/python

from datetime import datetime, tzinfo, timedelta
import sys

ZERO = timedelta(0)

class TZ(tzinfo):
    """Fixed offset in minutes east from UTC."""

    def __init__(self, offset, name):
        self.__offset = timedelta(minutes = offset)
        self.__name = name

    def utcoffset(self, dt):
        return self.__offset

    def tzname(self, dt):
        return self.__name

    def dst(self, dt):
        return ZERO


ms = int(sys.argv[1])
utc_time = datetime.fromtimestamp(ms//1000, TZ(0, "UTC"))
est_time = datetime.fromtimestamp(ms//1000, TZ(-5*60, "EST"))
edt_time = datetime.fromtimestamp(ms//1000, TZ(-4*60, "EDT"))

# print str(time.strftime("%Y-%m-%d %H:%M:%S.")) + '%03d' + 'Z%s' % (ms%1000, str(time.strftime("%z")))
print str(utc_time.strftime("%Z: %Y-%m-%d %H:%M:%S %z"))
print str(est_time.strftime("%Z: %Y-%m-%d %H:%M:%S %z"))
print str(edt_time.strftime("%Z: %Y-%m-%d %H:%M:%S %z"))
