package com.eviware.soapui.analytics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by aleshin on 5/15/2014.
 */

public class AnalyticsManager {

    public static final String SESSION_STARTED_ACTION = "Started";
    public static final String SESSION_FINISHED_ACTION = "Finished";
    private static AnalyticsManager instance = null;
    List<AnalyticsProvider> providers = new ArrayList<AnalyticsProvider>();
    List<AnalyticsProvider.ActionDescription> actions = new ArrayList<AnalyticsProvider.ActionDescription>();

    private String sessionId;
    private List<AnalyticsProviderFactory> factories = new ArrayList<AnalyticsProviderFactory>();

    AnalyticsManager() {

        Date date = new Date();
        String newString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        sessionId = String.format("AutoGeneratedSessionId:%s", newString);
    }

    public static AnalyticsManager initialize() {
        if (instance == null) {
            instance = new AnalyticsManager();
        }

        return getAnalytics();
    }

    public static AnalyticsManager getAnalytics() {

        if (instance == null)
            initialize();

        return instance;
    }

    public void trackAction(String action, Map<String, String> params) {
        trackAction(Category.Action, action, params);
    }

    public boolean trackAction(String actionName) {
        return this.trackAction(Category.Action, actionName, null);
    }
    // Single param action
    public boolean trackAction(String actionName, String paramName, String value) {

        Map<String, String> params = new HashMap<String, String>();
        params.put(paramName, value);

        return this.trackAction(Category.Action, actionName, params);
    }

    public boolean trackStartupAction(String actionName) {
        return this.trackAction(Category.Session, actionName, null);
    }

    protected void registerActiveProvider(AnalyticsProvider provider, boolean keepTheOnlyOne) {
        if (keepTheOnlyOne) {
            providers.clear();
        }
        providers.add(provider);
    }

    public void registerAnalyticsProviderFactory(AnalyticsProviderFactory factory) {
        factories.add(factory);
        // if (factories.size() == 1) {
        registerActiveProvider(factory.allocateProvider(), false);
        // }
    }

    public boolean selectAnalyticsProvider(String name, boolean keepTheOnlyOne) {
        for (int i = 0; i < factories.size(); i++) {
            AnalyticsProviderFactory factory = factories.get(i);
            if (factory.getName().compareToIgnoreCase(name) == 0) {
                registerActiveProvider(factory.allocateProvider(), keepTheOnlyOne);
                return true;
            }
        }
        if (keepTheOnlyOne) {
            // A way to stop logging
            providers.clear();
        }
        return false;
    }

    private boolean trackAction(Category category, String action, Map<String, String> params) {

        if (providers.isEmpty())
            return false;

        AnalyticsProvider.ActionDescription actionDescr = new AnalyticsProvider.ActionDescription(sessionId, category, action, params);

        for (int i = 0; i < providers.size(); i++) {
            try {
                providers.get(i).trackAction(actionDescr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return providers.size() > 0;
    }

    public enum Category {Unassigned, Session, Action}
}
