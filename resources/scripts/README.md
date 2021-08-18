The `load_content.py` script provides a simple way to load or delete a set of FHIR resources via OCL's FHIR service.
FHIR resources must be saved to a single directory and each FHIR resource must be saved in an individual file (not as a FHIR Bundle).

The `load_content.py` script accepts 5 parameters:
1. `operation_type` - Type of operation to perform for each FHIR resource:
    * POST - use this to POST FHIR resources to OCL (ie create new resources)
    * DELETE - use this to DELETE the FHIR resources from OCL; this makes it simple to test out loading and then deleting a set of resources
2. `ocl_api_token` - OCL API authentication token
3. `directory` - Directory where the files containing FHIR resources is stored
4. `ocl_env_url` - The OCL FHIR server url (This needs to be owner namespace url, so that we don't need to modify the
        original resource to add Accession Identifier)
5. (Optional) `force` (boolean) - by default false. If true, then uses database directly to delete the resource otherwise uses oclapi to delete the resource.

Examples:
1. Create CodeSystem resources from files stored in /Users/xyz/Documents/files/codesystems/ directory at http://localhost:8080/orgs/HL7/CodeSystem/
```
    python3 load_content.py post 1234567890abcdefghijklmnopqrstuvwxyz /Users/xyz/Documents/files/codesystems/ http://localhost:8080/orgs/HL7/CodeSystem/
```
2. Delete CodeSystem resources from files stored in /Users/xyz/Documents/files/codesystems/ directory at http://localhost:8080/orgs/HL7/CodeSystem/
```
    python3 load_content.py post 1234567890abcdefghijklmnopqrstuvwxyz /Users/xyz/Documents/files/codesystems/ http://localhost:8080/orgs/HL7/CodeSystem/
```
3. Create ValueSet resources from files stored in /Users/xyz/Documents/files/valuesets/ directory at http://localhost:8080/orgs/HL7/ValueSet/
```
    python3 load_content.py post 1234567890abcdefghijklmnopqrstuvwxyz /Users/xyz/Documents/files/valuesets/ http://localhost:8080/orgs/HL7/ValueSet/
```
4. Delete ValueSet resources from files stored in /Users/xyz/Documents/files/valuesets/ directory at http://localhost:8080/orgs/HL7/ValueSet/
```
    python3 load_content.py post 1234567890abcdefghijklmnopqrstuvwxyz /Users/xyz/Documents/files/valuesets/ http://localhost:8080/orgs/HL7/ValueSet/
```
