#!/bin/bash
cd /home/kavia/workspace/code-generation/device-remote-management-581-704/BackendServicesContainer
source venv/bin/activate
flake8 .
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
  exit 1
fi

