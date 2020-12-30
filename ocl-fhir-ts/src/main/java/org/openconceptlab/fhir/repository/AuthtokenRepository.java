package org.openconceptlab.fhir.repository;

import org.openconceptlab.fhir.model.AuthtokenToken;

public interface AuthtokenRepository extends BaseOclRepository<AuthtokenToken>{

    AuthtokenToken findByKey(String key);

}
