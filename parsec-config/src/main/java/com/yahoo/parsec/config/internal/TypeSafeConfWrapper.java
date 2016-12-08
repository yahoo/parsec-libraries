// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.config.internal;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.yahoo.parsec.config.ParsecConfig;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author guang001
 */
public class TypeSafeConfWrapper implements ParsecConfig{
    /**
     * delegate config to typesafe lib.
     */
    final Config configDelegate;

    /**
     * constructor.
     * @param conf delegate to typesafe
     */
    public TypeSafeConfWrapper(Config conf) {
        configDelegate = conf;
    }

    @Override
    public String getString(String key) {
        try {
            return configDelegate.getString(key);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isEmpty() {
        return configDelegate.isEmpty();
    }

    @Override
    public boolean getBoolean(String path) {
        try {
            return configDelegate.getBoolean(path);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Number getNumber(String path) {
        try {
            return configDelegate.getNumber(path);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getInt(String path) {
        try {
            return configDelegate.getInt(path);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getLong(String path) {
        try {
            return configDelegate.getLong(path);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double getDouble(String path) {
        try {
            return configDelegate.getDouble(path);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getDuration(String path, TimeUnit unit) {
        try {
            return configDelegate.getDuration(path, unit);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Duration getDuration(String path) {
        try {
            return configDelegate.getDuration(path);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Boolean> getBooleanList(String path) {
        try {
            return configDelegate.getBooleanList(path);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Number> getNumberList(String path) {
        try {
            return configDelegate.getNumberList(path);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Integer> getIntList(String path) {
        try {
            return configDelegate.getIntList(path);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Long> getLongList(String path) {
        try {
            return configDelegate.getLongList(path);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Double> getDoubleList(String path) {
        try {
            return configDelegate.getDoubleList(path);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getStringList(String path) {
        try {
            return configDelegate.getStringList(path);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<? extends ParsecConfig> getConfigList(String path) {
        try {
            List<TypeSafeConfWrapper> confWrappers = new ArrayList<>();
            List<? extends Config> configList = configDelegate.getConfigList(path);
            for (Config config : configList) {
                TypeSafeConfWrapper conf = new TypeSafeConfWrapper(config);
                confWrappers.add(conf);
            }
            return confWrappers;
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Long> getDurationList(String path, TimeUnit unit) {
        try {
            return configDelegate.getDurationList(path, unit);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Duration> getDurationList(String path) {
        try {
            return configDelegate.getDurationList(path);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getAnyRef(String path) {
        try {
            return configDelegate.getAnyRef(path);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<? extends Object> getAnyRefList(String path) {
        try {
            return configDelegate.getAnyRefList(path);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ParsecConfig getConfig(String path) {
        try {
            Config config = configDelegate.getConfig(path);
            return new TypeSafeConfWrapper(config);
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

}
