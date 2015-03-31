#!/bin/bash

#LOG=/tmp/$(basename $0).$(date +%Y%m%d-%H%M%S).$$.log
LOG=/dev/null

echo "autogen_pass called as: $0 $@" | tee -a ${LOG}

JSON_EDITOR="${1}"
APP_NAME="${2}"
LOGIN="${3}"
JSON_PATH="${4}"
DATABAG_FILE="${5}"
ADMIN_NAME="${6}"
ADMIN_EMAIL="${7}"
LENGTH=${8}

if [ -z "${JSON_EDITOR}" ] ; then
  echo "No JSON_EDITOR specified as first argument" | tee -a ${LOG}
  exit 2
fi

# if this is a brand new cloudstead, we might not have sendmail yet
if [ -z "$(which sendmail)" ] ; then
  echo "No sendmail found, installing postfix..." | tee -a ${LOG}
  apt-get install -y postfix
  echo "postfix installed"
fi

# check that password is not already set
current_pass="$(cat ${DATABAG_FILE} | ${JSON_EDITOR} -p ${JSON_PATH} | tr -d ' ')"
if [ ! -z "${current_pass}" ] ; then
  echo "Password already generated, exiting" | tee -a ${LOG}
  exit 0
fi

new_password="$(tr -dc A-Za-z0-0_ < /dev/urandom | head -c ${LENGTH})"
if [ -z "${new_password}" ] ; then
  echo "Empty password generated!" | tee -a ${LOG}
  exit 2
fi

backup=$(mktemp /tmp/json.XXXXXX)

cat ${DATABAG_FILE} > ${backup}
echo "Generated password, updating ${DATABAG_FILE} (path=${JSON_PATH})" | tee -a ${LOG}

cat ${DATABAG_FILE} | ${JSON_EDITOR} -p ${JSON_PATH} -v "${new_password}" -o ${DATABAG_FILE}
if [ $? -ne 0 ] ; then
  echo "Error updating databag ${DATABAG_FILE} with autogen password" | tee -a ${LOG}
  cp ${backup} ${DATABAG_FILE}
  exit 1
fi
echo "Databag updated, sending email" | tee -a ${LOG}

sendmail -oi -t 2> /tmp/autogen_$(date +%s) <<EOMAIL
From: do-not-reply@$(hostname)
To: ${ADMIN_EMAIL}
Subject: Auto-generated password for CloudOs app: ${APP_NAME}

Hello ${ADMIN_NAME},

This is an automated message from your private Cloudstead.

When the ${APP_NAME} app was recently installed, the password auto-generator was used to create a password.

* username: ${LOGIN}
* password: ${new_password}

It is highly recommended that you change this password immediately by logging into the ${APP_NAME} app
with the above username and password.

EOMAIL
rval=$?

if [ ${rval} -ne 0 ] ; then
    echo "Failed to send email, rolling back databag ${DATABAG_FILE}" | tee -a ${LOG}
    cp ${backup} ${DATABAG_FILE}
    exit 1
fi

echo "Email sent successfully, removing backup databag file" | tee -a ${LOG}
rm ${backup}
exit 0
