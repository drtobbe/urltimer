Apache Log UrlTimer
===================

In order to get accurate bitrate information, change apache LogFormat to contain response-time: %D

    LogFormat "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-Agent}i\" %D" combined
    LogFormat "%h %l %u %t \"%r\" %>s %b %D" common

Download, build and start urltimer binaries:

	$ mkdir -p /opt/urlanalysis
	$ cd /opt/urlanalysis
	$ git clone https://github.com/drtobbe/urltimer.git
	$ cd urltimer
	$ mvn package
	$ cd target
	$ nohup java -jar urltimer.war &
	$ open http://localhost:8181/urltimer/info

Download logstash binaries:

	$ cd /opt/urlanalysis
	$ wget https://download.elasticsearch.org/logstash/logstash/logstash-1.4.2.tar.gz
	$ tar xvfz logstash-1.4.2.tar.gz

Configure apache logfile path in logstash.conf and start logstash:
		
	$ vim ./urltimer/logstash/common-time-logstash.conf
	$ ./logstash-1.4.2/bin/logstash -f urltimer/logstash/common-time-logstash.conf -t
	$ nohup ./logstash-1.4.2/bin/logstash -f urltimer/logstash/common-time-logstash.conf &

