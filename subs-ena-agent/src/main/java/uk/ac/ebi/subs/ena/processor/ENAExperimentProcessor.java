package uk.ac.ebi.subs.ena.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.Archive;
import uk.ac.ebi.subs.data.status.ProcessingStatusEnum;
import uk.ac.ebi.subs.data.submittable.*;
import uk.ac.ebi.subs.ena.loader.SRALoaderService;
import uk.ac.ebi.subs.processing.ProcessingCertificate;
import uk.ac.ebi.subs.processing.SubmissionEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class ENAExperimentProcessor extends AbstractENAProcessor<ENAExperiment> {

    @Autowired
    public void setLoader(SRALoaderService<ENAExperiment> sraLoaderService) {
        this.sraLoaderService = sraLoaderService;
    }

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<ProcessingCertificate> processSubmission(SubmissionEnvelope envelope) {
        List<ProcessingCertificate> processingCertificateList = new ArrayList<>();
        final List<Assay> assays = envelope.getAssays();
        for (Assay assay : assays) {
            try {
                final ENAExperiment enaSubmittable = (ENAExperiment) convertFromSubmittableToENASubmittable(assay,new ArrayList<SingleValidationResult>());
                processingCertificateList.add((process(enaSubmittable)));
            } catch (IllegalAccessException e) {

            } catch (InstantiationException e) {

            }
        }
        return processingCertificateList;
    }

    @Override
    public String getSubmittableObjectTypeAsAString() {
        return Assay.class.getSimpleName();
    }

    @Override
    public ENASubmittable convertFromSubmittableToENASubmittable(Submittable submittable,Collection<SingleValidationResult> singleValidationResultList) throws InstantiationException, IllegalAccessException {
        final ENASubmittable enaSubmittable = BaseSubmittableFactory.create(ENAExperiment.class, submittable);
        singleValidationResultList.addAll(enaSubmittable.getValidationResultList());
        return enaSubmittable;
    }

}
