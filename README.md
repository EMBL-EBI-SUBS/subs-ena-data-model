# Submissions ENA Core Service

[![Build Status](https://travis-ci.org/EMBL-EBI-SUBS/subs-ena-core-service.svg?branch=master)](https://travis-ci.org/EMBL-EBI-SUBS/subs-ena-core-service)

This repository contains ENA's core data model and infrastructure.

## Notes

- #####ENA hold date (release date) discrepancy
    Specifying release dates in request to messages to ENA works differently for new and updated data. According to
    ENA's programmatic submission [guide](https://ena-docs.readthedocs.io/en/latest/submit/general-guide/programmatic.html#submission-xml-submit-studies-with-release-date),
    the HOLD date specified at the submission level affects all the objects in that submission. The SUBMISSION schema
    document [here](https://ena-docs.readthedocs.io/en/latest/submit/general-guide/programmatic.html#types-of-xml)
    tells that it is possible to add multiple HOLD dates inside the submission and that they can be set separately
    for individual objects by their 'accession' or 'refname' (which we assume includes object alias).  
    
    What has been observed in practice is that, although it is possible to add multiple HOLD dates in the submission request, they
    can only reference objects by their accessions only. The archive rejects the submission request
    if object is referenced by anything other than the accession.  
    
    This condition makes adding multiple HOLD dates for new objects impossible as they are never assigned an accession
    when they are first created. Interestingly, if multiple HOLD dates without accessions are added to the submission then the archive
    just picks the last one from the list and applies that to every object that has been referenced in the submission. Which
    would not be right in cases where objects would have different release dates.
    
    The workaround suggested by ENA for this issue is to only specify one HOLD date per submission. Which means that ,
    If there are multiple objects with distinct release dates then they should be sent separately inside their own
    submission message to the archive. Due to this, the agent submission service breaks down the single submission envelope
    that it receives from DSP into multiple ENA submissions based on how many objects with distinct release dates are in
    there, sends them to the archive one by one and then aggregates the results together before sending the response back to
    DSP.
    
    Things become simple when handling updates however. It is because objects that are to be updated come with accessions.
    So this time around it is possible to send just one submission with multiple HOLD dates referencing objects by their accessions
    and the archive will interpret them correctly thereby updating objects with the right hold date individually.
    Consequently, the ENA submission service then just sends a single submission message to the archive when its dealing with updates.

## License
This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE.md) file for details.