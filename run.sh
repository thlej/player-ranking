#!/usr/bin/env bash

up() {
    docker-compose build
    docker-compose up -d
}

down() {
    docker-compose down
}

$1