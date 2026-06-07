package com.capitalcruise.platform.referencedata.interfaces.rest.transform;

import com.capitalcruise.platform.referencedata.domain.model.resources.HelpTopic;
import com.capitalcruise.platform.referencedata.interfaces.rest.resources.HelpTopicResource;

public class HelpTopicResourceAssembler {

    private HelpTopicResourceAssembler() {
    }

    public static HelpTopicResource toResource(HelpTopic helpTopic) {
        return new HelpTopicResource(helpTopic.key(), helpTopic.title(), helpTopic.content());
    }
}
