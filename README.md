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
    are only allowed to reference objects by their accession numbers. The archive rejects the submission request
    if object is referenced by anything other than the accession.  
    
    This condition makes adding multiple HOLD dates for new objects impossible as they are never assigned an accession
    when they are first created. Interestingly, if multiple HOLD dates without accessions are added to the submission then the archive
    just picks the last one from the list and applies that to every object that has been referenced in the submission. Which
    would not be right in cases where objects might have different release dates.
    
    The currently implemented workaround for this limitation is a two step process. In the first step, all new objects
    (with or without release dates) are submitted to the archive in the usual way. Then, in the second step only those
    objects that have a non-null release date are then submitted to the archive but, only this time they are submitted
    as an updated object rather than as a new one as done in the previous step. It is so because after the first step
    all submitted objects then carry an accession number with them and as explained above it is quite easy to specify
    per object release date when we know the accession number.
    
    Things become simple when handling updates only. It is because objects that are to be updated come with accessions.
    So this time around it is possible to send just one submission with multiple HOLD dates referencing objects by their accessions
    and the archive will interpret them correctly thereby updating objects with the right hold date individually.
    Consequently, the submission service then just sends a single submission message to the archive when its dealing with updates.

## License
This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE.md) file for details.