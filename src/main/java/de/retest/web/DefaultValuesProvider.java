package de.retest.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import de.retest.ui.DefaultValueFinder;
import de.retest.ui.descriptors.IdentifyingAttributes;

public class DefaultValuesProvider implements DefaultValueFinder {

	public static final class ReadDefaultValuesException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public ReadDefaultValuesException( final Exception e ) {
			super( "Could not load default CSS values from " + DEFAULTS_FILE_PATH, e );
		}
	}

	public static final String DEFAULTS_FILE_PATH = "/defaults.yaml";

	private final Map<String, Map<String, String>> defaultValues;

	public DefaultValuesProvider() {
		try ( final InputStream url = getClass().getResourceAsStream( DEFAULTS_FILE_PATH ) ) {
			defaultValues = readAttributesConfigFromFile( url );
		} catch ( final Exception e ) {
			throw new ReadDefaultValuesException( e );
		}
	}

	private Map<String, Map<String, String>> readAttributesConfigFromFile( final InputStream in ) throws IOException {
		final Map<String, Map<String, String>> defaultValues = new HashMap<>();
		final ObjectMapper mapper = new ObjectMapper( new YAMLFactory() );
		final JsonNode jsonNode = mapper.readTree( in );
		for ( final Iterator<Entry<String, JsonNode>> elements = jsonNode.fields(); elements.hasNext(); ) {
			final Entry<String, JsonNode> field = elements.next();
			final Map<String, String> defaults = new HashMap<>();
			final ArrayNode valuesNode = (ArrayNode) field.getValue();
			for ( final Iterator<JsonNode> values = valuesNode.elements(); values.hasNext(); ) {
				final Entry<String, JsonNode> value = values.next().fields().next();
				defaults.put( value.getKey(), value.getValue().asText() );
			}
			defaultValues.put( field.getKey(), defaults );
		}
		return defaultValues;
	}

	@Override
	public Serializable getDefaultValue( final IdentifyingAttributes comp, final String attributesKey ) {
		final Map<String, String> defaults = defaultValues.get( comp.getType() );
		if ( defaults != null ) {
			return defaults.get( attributesKey );
		}
		return null;
	}

	public boolean isDefault( final String tag, final String attribute, final String attributeValue ) {
		final Map<String, String> defaults = defaultValues.get( tag );
		if ( defaults != null ) {
			final String defaultValue = defaults.get( attribute );
			if ( defaultValue != null ) {
				return defaultValue.equalsIgnoreCase( attributeValue );
			}
		}
		if ( attributeValue == null || attributeValue.trim().isEmpty() ) {
			return true;
		}
		if ( attributeValue.equals( "auto" ) ) {
			return true;
		}
		if ( attributeValue.equals( "none" ) ) {
			return true;
		}
		if ( attributeValue.equals( "normal" ) ) {
			return true;
		}
		if ( attributeValue.equals( "0px" ) ) {
			return true;
		}
		return false;
	}

}