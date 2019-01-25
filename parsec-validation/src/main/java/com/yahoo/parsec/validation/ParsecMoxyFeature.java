package com.yahoo.parsec.validation;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.InternalProperties;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;

import javax.ws.rs.Priorities;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

public class ParsecMoxyFeature implements Feature {

    private static final String JSON_FEATURE = MoxyJsonFeature.class.getSimpleName();

    @Override
    public boolean configure(FeatureContext context) {
        final Configuration config = context.getConfiguration();

        if (CommonProperties.getValue(config.getProperties(), config.getRuntimeType(),
                CommonProperties.MOXY_JSON_FEATURE_DISABLE, Boolean.FALSE, Boolean.class)) {
            return false;
        }

        final String jsonFeature = CommonProperties.getValue(config.getProperties(), config.getRuntimeType(),
                InternalProperties.JSON_FEATURE, JSON_FEATURE, String.class);
        // Other JSON providers registered.
        if (!JSON_FEATURE.equalsIgnoreCase(jsonFeature)) {
            return false;
        }

        // Disable other JSON providers.
        context.property(PropertiesHelper.getPropertyNameForRuntime(InternalProperties.JSON_FEATURE,
                config.getRuntimeType()), JSON_FEATURE);

        final int workerPriority = Priorities.USER + 3000;
        context.register(ParsecMoxyProvider.class, workerPriority);

        return true;
    }
}
