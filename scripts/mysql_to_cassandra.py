#!/usr/bin/python

import MySQLdb
import datetime

from pycassa.pool import ConnectionPool
from pycassa.columnfamily import ColumnFamily

# connect to mysql
db = MySQLdb.connect(host="localhost", user="root", passwd="root", db="metrink")

# connect to cassandra
pool = ConnectionPool('metrink')

# get the column family
col_fam = ColumnFamily(pool, 'metrics')


# you must create a Cursor object. It will let
#  you execute all the query you need
cur = db.cursor() 

# Use all the SQL you like
cur.execute('select company, client, device, groupName, name, time_stamp, value from metrics join metrics_devices on metrics.device_id = metrics_devices.device_id join metrics_groups on metrics.group_id = metrics_groups.group_id join metrics_names on metrics.name_id = metrics_names.name_id join metrics_owners on metrics.ownerId = metrics_owners.ownerId')

# print all the first cell of all the rows
for row in cur.fetchall() :
    time = datetime.datetime.fromtimestamp(row[5]//1000)
    time_str = str(time.strftime("%Y%m"))
    row_key = str(row[0]) + ":" + str(row[1]) + ":" + time_str + ":" +  str(row[2]) + ":" +  str(row[3]) + ":" +  str(row[4])

    print row_key
    
    col_fam.insert(row_key, { row[5]: row[6] })
    
# close our cassandra connection
pool.dispose()
    
# close our connection to mysql
db.close()