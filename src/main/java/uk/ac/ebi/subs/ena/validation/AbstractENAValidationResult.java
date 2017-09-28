package uk.ac.ebi.subs.ena.validation;

import uk.ac.ebi.subs.data.submittable.ENASubmittable;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;

import java.util.UUID;

/**
 * Created by neilg on 14/06/2017.
 */
public class AbstractENAValidationResult extends SingleValidationResult {

    public AbstractENAValidationResult(ENASubmittable enaSubmittable, String message) {
        super(ValidationAuthor.Ena, enaSubmittable.getId().toString());
        setMessage(message);
        this.setValidationStatus(SingleValidationResultStatus.Error);
        this.setEntityUuid(enaSubmittable.getId().toString());
    }
}
