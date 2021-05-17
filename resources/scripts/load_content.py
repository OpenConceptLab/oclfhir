#!/usr/bin/env python
'''
Script to Load the FHIR resources through OCL FHIR Service. It expects 4 parameters:
    1. type of operation:
        POST - creates new resource
        DELETE - deletes the resource and resource's HEAD version
    2. authentication token
    3. Directory where the files containing FHIR resources is stored
    4. The OCL FHIR server url (This needs to be owner namespace url, so that we don't need to modify the
        original resource to add Accession Identifier)
    5. Optional argument - force - by default false. If true, then uses database directly to delete the resource otherwise uses oclapi to delete the resource.
Examples:
1. Create CodeSystem resources from files stored in /Users/xyz/Documents/files/codesystems/ directory at http://localhost:8080/orgs/HL7/CodeSystem/
    python3 load_content.py post 891b4b17feab99f3ff7e5b5d04ccc5da7aa96da6 /Users/xyz/Documents/files/codesystems/ http://localhost:8080/orgs/HL7/CodeSystem/
2. Delete CodeSystem resources from files stored in /Users/xyz/Documents/files/codesystems/ directory at http://localhost:8080/orgs/HL7/CodeSystem/
    python3 load_content.py post 891b4b17feab99f3ff7e5b5d04ccc5da7aa96da6 /Users/xyz/Documents/files/codesystems/ http://localhost:8080/orgs/HL7/CodeSystem/
3. Create ValueSet resources from files stored in /Users/xyz/Documents/files/valuesets/ directory at http://localhost:8080/orgs/HL7/ValueSet/
    python3 load_content.py post 891b4b17feab99f3ff7e5b5d04ccc5da7aa96da6 /Users/xyz/Documents/files/valuesets/ http://localhost:8080/orgs/HL7/ValueSet/
4. Delete ValueSet resources from files stored in /Users/xyz/Documents/files/valuesets/ directory at http://localhost:8080/orgs/HL7/ValueSet/
    python3 load_content.py post 891b4b17feab99f3ff7e5b5d04ccc5da7aa96da6 /Users/xyz/Documents/files/valuesets/ http://localhost:8080/orgs/HL7/ValueSet/

NOTES:
 - Files can be stored at any directory. Only thing expected is the is one resource stored per file.

'''

import os
import sys
import time
import logging
import os.path
import re
import json
import requests

# Input Arguments
operation=sys.argv[1]
auth_token=sys.argv[2]
input_dir=sys.argv[3]
url=sys.argv[4]
force=sys.argv[5] if len(sys.argv) >= 6 else 'false'
headers={'Content-Type': 'application/json', 'Authorization': 'Token ' + auth_token}

def run(fhir_str_list):
    retry_post=list()
    for fhir_str in fhir_str_list:
        resource=json.loads(fhir_str)
        resource_type=resource['resourceType']
        res_id=resource['id']
        version=resource['version']
        if res_id and version:
            text=resource_type + ' of id=' + res_id + ' ,version=' + version + '. '
            if operation.lower() == 'post':
                try:
                    post(fhir_str, text)
                except Exception as e:
                    print('Error creating ' + resource_type + 'of id=' + res_id + '. It will be retried again.' + str(e))
                    retry_post.append(fhir_str)
            if operation.lower() == 'delete':
                delete(resource_type, res_id, version)
                delete(resource_type, res_id, 'HEAD')
    return retry_post

def get_fhir_str():
    fhir_str_list=list()
    for root, directories, files in os.walk(input_dir):
        for file in files:
            if not file.startswith('.'):
                with open(os.path.join(root,file), 'r') as content:
                    fhir_str_list.append(content.read())
                    content.close()
    return fhir_str_list

def post(fhir_str, text):
    r = requests.post(url, headers=headers, data=fhir_str, timeout=15)
    if(r.status_code == 201):
        print('Created ' + text)
    elif(r.status_code == 409):
        print('Already Exists ' + text)
    else:
        print('Error creating ' + text + r.text)

def delete(resource_type, res_id, version):
    text=resource_type + ' of id=' + res_id + ' ,version=' + version + '. '
    delete_url = url + '/' + res_id + '/version/' + version + '/?force=' + force
    r = requests.delete(delete_url, headers=headers)
    if(r.status_code == 204):
        print('Deleted ' + text)
    else:
        print('Error deleting ' + text + r.text)

def main():
    fhir_str_list=get_fhir_str()
    retry = run(fhir_str_list)
    if retry:
        run(retry)


if __name__ == "__main__":
    main()



