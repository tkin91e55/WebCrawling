#!/bin/bash

myarr=($(tail -n+2 config.csv | awk-csv-parser --output-separator=',' | grep SearchCrit | awk -F"," '{print $2}')) #this work
