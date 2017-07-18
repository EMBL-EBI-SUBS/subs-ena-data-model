package uk.ac.ebi.subs.ena.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.subs.data.component.Archive;
import uk.ac.ebi.subs.data.status.ProcessingStatusEnum;
import uk.ac.ebi.subs.data.submittable.ENASubmittable;
import uk.ac.ebi.subs.ena.loader.SRALoaderService;
import uk.ac.ebi.subs.processing.ProcessingCertificate;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationAuthor;
import uk.ac.ebi.subs.validator.data.ValidationStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by neilg on 19/05/2017.
 */
public abstract class AbstractENAProcessor<T extends ENASubmittable> implements ENAAgentProcessor<T> {

    protected static final Logger logger = LoggerFactory.getLogger(ENAStudyProcessor.class);
    protected SRALoaderService<T> sraLoaderService;
    protected DataSource dataSource;

    @Override
    public SRALoaderService<T> getLoader() {
        return sraLoaderService;
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    @Transactional
    public ProcessingCertificate process(T submittable) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        ProcessingCertificate processingCertificate = new ProcessingCertificate(submittable, Archive.Ena, ProcessingStatusEnum.Error);
        try {
            sraLoaderService.executeSubmittableSRALoader(submittable,submittable.getAlias(),connection);
            processingCertificate = new ProcessingCertificate(submittable, Archive.Ena, ProcessingStatusEnum.Received,submittable.getAccession());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                DataSourceUtils.doReleaseConnection(connection,dataSource);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return processingCertificate;
    }

    /**
     * Validates the passed submittable entity via the SRA validator.
     *
     * @param enaSubmittable entity to validate
     * @return {@link ValidationResult} generated by the SRA validator
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public ValidationResult getValidationResult(T enaSubmittable) throws InstantiationException, IllegalAccessException {
        process(enaSubmittable);

        return sraLoaderService.getValidationResult();
    }

    /**
     * Gets the validation messages via the SRA Validator.
     *
     * @param enaSubmittable entity to validate
     * @return a {@link Collection} of {@link ValidationMessage}s if the validation erred or an empty list.
     */
    public Collection<SingleValidationResult> validateEntity(T enaSubmittable) {
        logger.info("Validation started for {} entity with id: {}", enaSubmittable.getClass().getSimpleName(),
                enaSubmittable.getId());

        Collection<SingleValidationResult> singleValidationResultCollection = new ArrayList<>();
        try {
            ValidationResult validationResult = getValidationResult(enaSubmittable);
            singleValidationResultCollection.addAll(convertValidationMessages(enaSubmittable.getId().toString(), validationResult));
            final Collection<ValidationMessage<Origin>> messages = validationResult.getMessages();
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("An exception occured: {}", e.getMessage());
            SingleValidationResult singleValidationResult = new SingleValidationResult(ValidationAuthor.Ena,enaSubmittable.getId().toString());
            singleValidationResult.setMessage(e.getMessage());
            singleValidationResult.setValidationStatus(ValidationStatus.Error);
            singleValidationResultCollection.add(singleValidationResult);
        }

        return singleValidationResultCollection;
    }

    /**
     * Cast a CheckedException as an unchecked one.
     *
     * @param throwable to cast
     * @param <T>       the type of the Throwable
     * @return this method will never return a Throwable instance, it will just throw it.
     * @throws T the throwable as an unchecked throwable
     */
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> RuntimeException rethrow(Throwable throwable) throws T {
        throw (T) throwable; // rely on vacuous cast
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Converts a collection of SRA validation messages to a collection {@link SingleValidationResult}
     * @param {@link Collection} of {@link ValidationMessage}
     * @return {@link Collection} of {@link SingleValidationResult}
     */
    Collection<SingleValidationResult> convertValidationMessages (String entityUUID, ValidationResult validationResult) {
        Collection<SingleValidationResult> singleValidationResultCollection = new ArrayList<>();
        for (ValidationMessage<Origin> validationMessage : validationResult.getMessages()) {
            SingleValidationResult singleValidationResult = new SingleValidationResult(ValidationAuthor.Ena,entityUUID);
            singleValidationResult.setMessage(validationMessage.getMessage());
            if (validationMessage.getSeverity().equals(Severity.ERROR)) {
                singleValidationResult.setValidationStatus(ValidationStatus.Error);
            } else if (validationMessage.getSeverity().equals(Severity.WARNING)) {
                singleValidationResult.setValidationStatus(ValidationStatus.Warning);
            } else if (validationMessage.getSeverity().equals(Severity.FIX)) {
                singleValidationResult.setValidationStatus(ValidationStatus.Error);

            }
        }
        return singleValidationResultCollection;
    }
}
