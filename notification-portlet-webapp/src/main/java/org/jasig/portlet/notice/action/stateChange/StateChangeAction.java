/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portlet.notice.action.stateChange;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletPreferences;
import org.jasig.portlet.notice.NotificationAction;
import org.jasig.portlet.notice.NotificationEntry;
import org.jasig.portlet.notice.NotificationState;
import org.jasig.portlet.notice.service.CacheNotificationService;
import org.jasig.portlet.notice.service.jpa.JpaNotificationService;
import org.jasig.portlet.notice.util.SpringContext;

/**
 *
 * @author mglazier
 */
public class StateChangeAction extends NotificationAction {

    public StateChangeAction() {
        // Set a default label;  most use cases will use the setter and override
        setLabel("SET-STATE");
    }

    public StateChangeAction(String label) {
        setLabel(label);
    }

    /**
     * When invoke is called, a configured notification state is set for the entry if it has not already been set.
     * {@link JpaNotificationService} and {@link CacheNotificationService} are used here to add the entry state and clear
     * the cache for the user.  This class is not managed by Spring, so these objects must be obtained using the 
     * Spring context that {@SpringContext} provides.
     * 
     * @param req
     * @param res
     * @throws IOException 
     */
    @Override
    public void invoke(final ActionRequest req, final ActionResponse res) throws IOException {
        JpaNotificationService jpaService = (JpaNotificationService) SpringContext.getApplicationContext().getBean("jpaNotificationService");
        CacheNotificationService cacheService = (CacheNotificationService) SpringContext.getApplicationContext().getBean("cacheNotificationService");

        final PortletPreferences prefs = req.getPreferences();
        final String clickedState = prefs.getValue("StateChangeNotificationServiceDecorator.applyState", "COMPLETED");
        NotificationState notificationState = NotificationState.valueOf(clickedState);

        boolean stateFound = false;

        final NotificationEntry entry = getTarget();
        Map<NotificationState, Date> stateMap = entry.getStates();
        if (stateMap != null && stateMap.size() > 0) {
            for (NotificationState state : stateMap.keySet()) {
                if (state == notificationState) {
                    stateFound = true;
                }
            }
        }

        if (!stateFound) {
            jpaService.addEntryState(req, entry.getId(), notificationState);
            cacheService.clearCacheForUser(req);
        }

        res.sendRedirect(entry.getUrl());
    }
}
