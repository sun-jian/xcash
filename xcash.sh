#!/bin/bash

###
# chkconfig: 345 20 80
# description: Vert.x application service script
# processname: java
#
# Installation (CentOS):
# copy file to /etc/init.d
# chmod +x /etc/init.d/my-vertx-application
# chkconfig --add /etc/init.d/my-vertx-application
# chkconfig my-vertx-application on
#
# Installation (Ubuntu):
# copy file to /etc/init.d
# chmod +x /etc/init.d/my-vertx-application
# update-rc.d my-vertx-application defaults
#
#
# Usage: (as root)
# service my-vertx-application start
# service my-vertx-application stop
# service my-vertx-application status
#
###

# The directory in which your application is installed
APPLICATION_DIR="/usr/local/xcash"
# The fat jar containing your application
APPLICATION_JAR="xcash-fat.jar"
# The application argument such as -Dfoo=bar ...
APPLICATION_ARGS="-Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory -Dvertx.disableDnsResolver=true"
# The Java command to use to launch the application (must be java 8+)
#JAVA=/opt/java/java/bin/java
# This service name
APPLICATION_NAME="XCach Service"

# ***********************************************
LOG_FILE="${APPLICATION_DIR}/xcash.log"
RUNNING_PID="${APPLICATION_DIR}"/RUNNING_PID
# ***********************************************

# colors
red='\e[0;31m'
green='\e[0;32m'
yellow='\e[0;33m'
reset='\e[0m'

echoRed() { echo -e "${red}$1${reset}"; }
echoGreen() { echo -e "${green}$1${reset}"; }
echoYellow() { echo -e "${yellow}$1${reset}"; }

# Check whether the application is running.
# The check is pretty simple: open a running pid file and check that the process
# is alive.
isrunning() {
  # Check for running app
  if [ -f "$RUNNING_PID" ]; then
    proc=$(cat $RUNNING_PID);
    if /bin/ps -p $proc 1>&2 >/dev/null;
    then
      return 0
    fi
  fi
  return 1
}

start() {
  if isrunning; then
    echoYellow "The Vert.x application is already running"
    return 0
  fi

  pushd $APPLICATION_DIR > /dev/null
  nohup java $APPLICATION_ARGS -jar $APPLICATION_DIR/$APPLICATION_JAR > $LOG_FILE 2>&1 &
  echo $! > ${RUNNING_PID}
  popd > /dev/null

  if isrunning; then
    echoGreen "$APPLICATION_NAME started"
    exit 0
  else
    echoRed "$APPLICATION_NAME has not started - check log"
    exit 3
  fi
}

restart() {
  stop
  echo "$APPLICATION_NAME stopped"
  start
  echo "$APPLICATION_NAME started"
}

stop() {
  if isrunning; then
    kill `cat $RUNNING_PID`
    rm $RUNNING_PID
    echoRed "$APPLICATION_NAME stopped"
  fi
}

status() {
  if isrunning; then
    echoGreen "$APPLICATION_NAME is running"
  else
    echoRed "$APPLICATION_NAME is either stopped or inaccessible"
  fi
}

case "$1" in
start)
    start
;;

status)
   status
   exit 0
;;

stop)
    if isrunning; then
    stop
    exit 0
    else
    echoRed "$APPLICATION_NAME is not running"
    exit 3
    fi
;;

restart)
    stop
    start
;;

*)
    echo "Usage: $0 {status|start|stop|restart}"
    exit 1
esac
