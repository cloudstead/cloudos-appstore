#!/bin/bash

APP_NAME=${1}
LOGIN=${2}
PATH=${3}
DATABAG_FILE=${4}
ADMIN_NAME=${5}
ADMIN_EMAIL=${6}
LENGTH=${7}

export PATH=/bin:/usr/bin:/usr/local/bin:${PATH}
new_password="$(tr -dc A-Za-z0-0_ < /dev/urandom | head -c ${LENGTH})"

# sanity check
if [ -z "${new_password}" ] ; then
  echo "Empty password generated!"
  exit 2
fi

backup=$(mktemp /tmp/json.XXXXXX)

cat ${DATABAG_FILE} > ${backup}

if ! $(cat ${DATABAG_FILE} | cos json -o write -p '${PATH}' -v '"'${new_password}'"' -w ${DATABAG_FILE}) ; then
  echo "Error updating databag ${DATABAG_FILE} with autogen password"
  cp ${backup} ${DATABAG_FILE}
  exit 1
fi

sendmail -oi -t 2> /tmp/autogen_$(date +%s) <<EOMAIL
From: do-not-reply@$(hostname).strip}
To: ${ADMIN_EMAIL}
Subject: Auto-generated password for CloudOs app: #{app[:name]}

Hello ${ADMIN_NAME},

This is an automated message from your private Cloudstead.

When the #{APP_NAME} app was recently installed, the password auto-generator was used to create a password.

* username: ${LOGIN}
* password: ${new_password}

It is highly recommended that you change this password immediately by logging into the ${APP_NAME} app
with the above username and password.

EOMAIL
rval=$?

if [ ${rval} -eq 0 ] ; then
    cp ${backup} ${DATABAG_FILE}
    exit 1
fi

rm ${backup}
