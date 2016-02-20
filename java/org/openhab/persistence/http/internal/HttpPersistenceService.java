/**
 * Copyright (c) 2010-2016, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.persistence.http.internal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
import java.util.IllegalFormatException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openhab.core.items.Item;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.types.State;
import org.openhab.io.net.http.HttpUtil;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a persistence engine using HTTP calls.
 *
 *
 * @author Gijs de Vries
 * @since 1.9.1
 */
public class HttpPersistenceService implements PersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(HttpPersistenceService.class);

    private String urlPattern;
    private boolean forceNumeric = false;
    private boolean initialized = false;

    public void activate(final BundleContext bundleContext, final Map<String, Object> config) {
        logger.debug("http persistence bundle starting.");
        urlPattern = (String) config.get("pattern");
        if (StringUtils.isBlank(urlPattern)) {
            logger.warn("The URL is missing - please configure the httppersistence:pattern parameter in openhab.cfg");
            return;
        }
        forceNumeric = Arrays.asList("yes", "on", "true").contains(((String) config.get("forcenumeric")).toLowerCase());
        initialized = true;
    }

    public void deactivate(final int reason) {
        logger.debug("http persistence bundle stopping.");
        initialized = false;
    }

    @Override
    public String getName() {
        return "httppersistence";
    }

    @Override
    public void store(Item item) {
        store(item, null);
    }

    @Override
    public void store(Item item, String name) {
        if (!initialized) {
            return;
        }
        if (name == null) {
            name = item.getName();
        }
        Object value = null;
        if (forceNumeric) {
            value = castToDouble(item);
        } else {
            value = item.getState().toString();
        }
        if (value != null) {
            try {
                String url = String.format(urlPattern, URLEncoder.encode(name, "UTF-8"), value, new Date());
                logger.debug("Calling " + url);
                HttpUtil.executeUrl("GET", url, 15);
            } catch (UnsupportedEncodingException e) {
                logger.error("No UTF8 encoding avaliable.", e);
            } catch (IllegalFormatException ife) {
                logger.error("Error constructing URL using '" + urlPattern + "'. " + ife.getMessage(), ife);
            }
        }
    }

    private Double castToDouble(Item item) {
        State state = item.getState();
        if (state instanceof Number) {
            return ((Number) state).doubleValue();
        } else if (state instanceof Enum<?>) {
            return Double.valueOf(((Enum<?>) state).ordinal());
        }
        logger.info("Unsupported type '" + state.getClass() + "' in " + item.getName());
        return null;
    }

}
