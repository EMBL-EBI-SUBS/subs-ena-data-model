package uk.ac.ebi.subs.ena.submission;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.ac.ebi.ena.sra.xml.ID;
import uk.ac.ebi.ena.sra.xml.RECEIPTDocument;
import uk.ac.ebi.ena.sra.xml.SUBMISSIONSETDocument;
import uk.ac.ebi.ena.sra.xml.SubmissionType;
import uk.ac.ebi.subs.data.submittable.*;
import uk.ac.ebi.subs.ena.action.*;
import uk.ac.ebi.subs.ena.http.UniRestWrapper;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FullSubmissionService {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private List<ActionService> actionServiceList = new ArrayList<>();
    private UniRestWrapper uniRestWrapper;


    public FullSubmissionService(UniRestWrapper uniRestWrapper,
                                 List<ActionService> actionServiceList,
                                 @Value("${ena.action_services}") String [] enaProcessorNames) {
        this.uniRestWrapper = uniRestWrapper;

        if (enaProcessorNames != null && enaProcessorNames.length > 0) {
            final Map<String, ActionService> actionServiceMap =
                    actionServiceList.stream()
                            .collect(Collectors.toMap(a -> a.getClass().getSimpleName(), c -> c));
            this.actionServiceList = Arrays.asList(enaProcessorNames).stream()
                    .map(p -> actionServiceMap.get(p))
                    .collect(Collectors.toList());
        } else {
            this.actionServiceList = actionServiceList;
        }

    }

    public RECEIPTDocument.RECEIPT submit (
            String submissionAlias,
            String centerName,
            Map<Class<? extends ActionService>,Object> paramMap,
            List<SingleValidationResult> singleValidationResults) throws XmlException, IOException, TransformerException {
        final SUBMISSIONSETDocument submissionsetDocument = SUBMISSIONSETDocument.Factory.newInstance();
        final SubmissionType submissionType = submissionsetDocument.addNewSUBMISSIONSET().addNewSUBMISSION();
        final SubmissionType.ACTIONS actions = submissionType.addNewACTIONS();
        submissionType.setAlias(submissionAlias);
        submissionType.setCenterName(centerName);
        Map<String, UniRestWrapper.Field> parameterMap = new HashMap<>();
        Map<String, Map<String,Submittable>> schemaAliasMapMap = new HashMap<>();

        int i = 0;

        List<SubmissionType.ACTIONS.ACTION> actionList = new ArrayList<>();

        for (ActionService actionService : actionServiceList) {

            final Object actionServiceParam = paramMap.get(actionService.getClass());

            final SubmissionType.ACTIONS.ACTION actionXML = actionService.createActionXML(actionServiceParam);
            if (actionXML != null) {
                    actionList.add(actionXML);
            }

            if (actionService instanceof SubmittablesActionService) {
                SubmittablesActionService submittablesActionService = (SubmittablesActionService) actionService;
                Submittable[] submittables = (Submittable[]) actionServiceParam;
                if (submittables != null && submittables.length > 0) {

                    Map<String,Submittable> submittableMap = new HashMap<>();

                    for (Submittable submittable : submittables) {
                        submittableMap.put(ENASubmittable.getENAAlias(submittable.getAlias(),submittable.getTeam().getName()),submittable);
                    }
                    schemaAliasMapMap.put(submittablesActionService.getSchemaName(),submittableMap);

                    InputStream xmlInputStream = submittablesActionService.getXMLInputStream(submittables,singleValidationResults);parameterMap.put(submittablesActionService.getSchemaName().toUpperCase(), new UniRestWrapper.Field(
                            submittablesActionService.getSchemaName() + ".xml", xmlInputStream)); }
                }
        }



        final SubmissionType.ACTIONS.ACTION[] newActions = actionList.toArray(new SubmissionType.ACTIONS.ACTION[actionList.size()]);
        actions.setACTIONArray(newActions);
        logger.info(submissionsetDocument.xmlText());
        final InputStream submittableInputStream = IOUtils.toInputStream(submissionsetDocument.xmlText(), Charset.forName("UTF-8"));
        parameterMap.put("SUBMISSION", new UniRestWrapper.Field("submission.xml", submittableInputStream));
        final String receiptString = uniRestWrapper.postJson(parameterMap);
        logger.info(receiptString);
        final RECEIPTDocument receiptDocument = RECEIPTDocument.Factory.parse(receiptString);
        updateAccession(receiptDocument.getRECEIPT().getSTUDYArray(),schemaAliasMapMap.get(StudyActionService.SCHEMA));
        updateAccession(receiptDocument.getRECEIPT().getSAMPLEArray(),schemaAliasMapMap.get(SampleActionService.SCHEMA));
        updateAccession(receiptDocument.getRECEIPT().getEXPERIMENTArray(),schemaAliasMapMap.get(AssayActionService.SCHEMA));
        updateAccession(receiptDocument.getRECEIPT().getRUNArray(),schemaAliasMapMap.get(AssayDataActionService.SCHEMA));
        updateAccession(receiptDocument.getRECEIPT().getANALYSISArray(),schemaAliasMapMap.get(SequenceVariationAnalysisActionService.SCHEMA));

        return receiptDocument.getRECEIPT();
    }

    private void updateAccession (ID[] ids, Map<String, Submittable> submittableMap) {
        if (submittableMap != null && ids != null) {
            for (ID id : ids) {
                final Submittable submittable = submittableMap.get(id.getAlias());
                if (submittable != null && id.getAccession() != null)
                    submittable.setAccession(id.getAccession());
            }
        }
    }

}
