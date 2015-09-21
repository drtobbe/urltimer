Apache Log UrlTimer
===================

In order to get accurate bitrate information, change apache LogFormat to contain response-time: %D

    LogFormat "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" %D" combined
    LogFormat "%h %l %u %t \"%r\" %>s %b %D" common

Download, build and start urltimer:

	$ mkdir -p /opt/urlanalysis
	$ cd /opt/urlanalysis
	$ git clone https://github.com/drtobbe/urltimer.git
	$ cd urltimer
	$ mvn package
	$ cp target/urltimer.jar .
	$ nohup bash urltimer.jar &
	$ open http://localhost:8181/urltimer/info

Download logstash binaries:

	$ cd /opt/urlanalysis
	$ wget https://download.elasticsearch.org/logstash/logstash/logstash-1.4.2.tar.gz
	$ tar xvfz logstash-1.4.2.tar.gz

Configure apache logfile path in logstash.conf and start logstash:
		
	$ vim ./urltimer/logstash/common-time-logstash.conf
	$ ./logstash-1.4.2/bin/logstash -f urltimer/logstash/common-time-apache.conf -t
	$ nohup ./logstash-1.4.2/bin/logstash -f urltimer/logstash/common-time-apache.conf &


Urltimer as Spring-Boot deamon:

	$ sudo ln -sf /opt/urlanalysis/urltimer/urltimer.jar /etc/init.d/urltimer
	$ sudo /etc/init.d/urltimer start
	$ tail -f /var/log/urltimer.log
	

Logstash with yum and epel:

	$ sudo vi /etc/yum.repos.d/logstash.repo

	[logstash-1.5]
	name=logstash repository for 1.5.x packages
	baseurl=http://packages.elasticsearch.org/logstash/1.5/centos
	gpgcheck=1
	gpgkey=http://packages.elasticsearch.org/GPG-KEY-elasticsearch
	enabled=1

	$ sudo yum -y install logstash
	$ sudo cp /opt/urlanalysis/urltimer/logstash/nginx-logstash.conf /etc/logstash/conf.d/.
	$ sudo chkconfig logstash on
	$ /opt/logstash/bin/logstash -f /etc/logstash/conf.d/nginx-logstash.conf -t
	$ sudo service logstash start


Log Tailer without Logstash

	export SQUID_LOGTAILER_FILENAME=/usr/local/squid/var/logs/access.log
	export APACHE_LOGTAILER_FILENAME=/var/log/apache2/access.log
