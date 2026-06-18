#!/bin/bash

ENDPOINT=http://localhost:4566

echo "Criando buckets..."

aws s3 mb s3://ticketwave-banners \
--endpoint-url=$ENDPOINT

aws s3 mb s3://ticketwave-tickets \
--endpoint-url=$ENDPOINT

echo "Criando filas..."

aws sqs create-queue \
--queue-name ticketwave-payment-queue.fifo \
--attributes FifoQueue=true,ContentBasedDeduplication=true \
--endpoint-url=$ENDPOINT

aws sqs create-queue \
--queue-name ticketwave-payment-dlq.fifo \
--attributes FifoQueue=true \
--endpoint-url=$ENDPOINT

echo "Criando tópicos SNS..."

aws sns create-topic \
--name ticket-confirmed \
--endpoint-url=$ENDPOINT

aws sns create-topic \
--name payment-approved \
--endpoint-url=$ENDPOINT

aws sns create-topic \
--name payment-declined \
--endpoint-url=$ENDPOINT

echo "Criando DynamoDB..."

aws dynamodb create-table \
--table-name audit_events \
--attribute-definitions \
AttributeName=entityType,AttributeType=S \
AttributeName=eventId,AttributeType=S \
--key-schema \
AttributeName=entityType,KeyType=HASH \
AttributeName=eventId,KeyType=RANGE \
--billing-mode PAY_PER_REQUEST \
--endpoint-url=$ENDPOINT

echo "Recursos AWS criados."