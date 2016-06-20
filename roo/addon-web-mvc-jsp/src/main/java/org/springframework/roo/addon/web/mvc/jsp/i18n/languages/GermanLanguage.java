package org.springframework.roo.addon.web.mvc.jsp.i18n.languages;

import java.io.InputStream;
import java.util.Locale;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.web.mvc.jsp.i18n.AbstractLanguage;
import org.springframework.roo.support.util.FileUtils;

/**
 * German language support.
 * 
 * @author Stefan Schmidt
 * @since 1.1
 */
@Component
@Service
public class GermanLanguage extends AbstractLanguage {

    public InputStream getFlagGraphic() {
        return FileUtils.getInputStream(getClass(), "de.png");
    }

    public String getLanguage() {
        return "Deubuilderh";
    }

    public Locale getLocale() {
        return Locale.GERMAN;
    }

    public InputStream getMessageBundle() {
        return FileUtils.getInputStream(getClass(), "messages_de.properties");
    }
}
