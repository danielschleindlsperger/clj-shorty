#!/bin/sh -l

set -e

sqitch deploy "${INPUT_DATABASE_URL}"