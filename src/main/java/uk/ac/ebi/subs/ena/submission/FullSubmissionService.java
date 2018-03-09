package uk.ac.ebi.subs.ena.submission;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.ac.ebi.ena.sra.xml.RECEIPTDocument;
import uk.ac.ebi.ena.sra.xml.SUBMISSIONSETDocument;
import uk.ac.ebi.ena.sra.xml.SubmissionType;
import uk.ac.ebi.subs.data.submittable.*;
import uk.ac.ebi.subs.ena.action.*;
import uk.ac.ebi.subs.ena.http.UniRestWrapper;

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
            Map<Class,Object> paramMap) throws Exception {
        final SUBMISSIONSETDocument submissionsetDocument = SUBMISSIONSETDocument.Factory.newInstance();
        final SubmissionType submissionType = submissionsetDocument.addNewSUBMISSIONSET().addNewSUBMISSION();
        final SubmissionType.ACTIONS actions = submissionType.addNewACTIONS();
        submissionType.setAlias(submissionAlias);
        submissionType.setCenterName(centerName);
        Map<String, UniRestWrapper.Field> parameterMap = new HashMap<>();

        int i = 0;

        List<SubmissionType.ACTIONS.ACTION> actionList = new ArrayList<>();

        for (ActionService actionService : actionServiceList) {
            final Object actionServiceParam = paramMap.get(actionService.getSubmittableClass());

            if (actionServiceParam != null) {

                final SubmissionType.ACTIONS.ACTION actionXML = actionService.createActionXML(actionServiceParam);
                if (actionXML != null) {
                    actionList.add(actionXML);
                }

                if (actionService instanceof SubmittablesActionService) {
                    SubmittablesActionService submittablesActionService = (SubmittablesActionService) actionService;
                    Submittable[] submittables = (Submittable[]) actionServiceParam;
                    if (submittables != null && submittables.length > 0) {
                        InputStream xmlInputStream = submittablesActionService.getXMLInputStream(submittables);
                        parameterMap.put(submittablesActionService.getSchemaName().toUpperCase(),
                                new UniRestWrapper.Field(
                                        submittablesActionService.getSchemaName() + ".xml",
                                        xmlInputStream));
                    }

                }
            }

        }

        final SubmissionType.ACTIONS.ACTION[] newActions = actionList.toArray(new SubmissionType.ACTIONS.ACTION[actionList.size()]);
        actions.setACTIONArray(newActions);
        final InputStream submittableInputStream = IOUtils.toInputStream(submissionsetDocument.xmlText(), Charset.forName("UTF-8"));
        parameterMap.put("SUBMISSION", new UniRestWrapper.Field("submission.xml", submittableInputStream));
        final String receiptString = uniRestWrapper.postJson(parameterMap);
        logger.info(receiptString);
        final RECEIPTDocument receiptDocument = RECEIPTDocument.Factory.parse(receiptString);
        return receiptDocument.getRECEIPT();
    }

}
