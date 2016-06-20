package org.springframework.roo.addon.web.mvc.jsp.menu;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.jsp.roundtrip.XmlRoundTripFileManager;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlElementBuilder;
import org.springframework.roo.support.util.XmlRoundTripUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.osgi.service.component.ComponentContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Generates the jsp menu and allows for management of menu items.
 * 
 * @author Stefan Schmidt
 * @since 1.0
 */
@Component
@Service
public class MenuOperationsImpl implements MenuOperations {
	
	protected final static Logger LOGGER = HandlerUtils.getLogger(MenuOperationsImpl.class);
	
	// ------------ OSGi component attributes ----------------
   	private BundleContext context;
   	
    private FileManager fileManager;
    private ProjectOperations projectOperations;
    private PropFileOperations propFileOperations;
    private XmlRoundTripFileManager xmlRoundTripFileManager;
    
   	protected void activate(final ComponentContext context) {
    	this.context = context.getBundleContext();
    }

    public void addMenuItem(final JavaSymbolName menuCategoryName,
            final JavaSymbolName menuItemId, final String globalMessageCode,
            final String link, final String idPrefix,
            final LogicalPath logicalPath) {
        addMenuItem(menuCategoryName, menuItemId, "", globalMessageCode, link,
                idPrefix, false, logicalPath);
    }

    private void addMenuItem(final JavaSymbolName menuCategoryName,
            final JavaSymbolName menuItemId, final String menuItemLabel,
            final String globalMessageCode, final String link, String idPrefix,
            final boolean writeProps, final LogicalPath logicalPath) {
        Validate.notNull(menuCategoryName, "Menu category name required");
        Validate.notNull(menuItemId, "Menu item name required");
        Validate.notBlank(link, "Link required");

        final Map<String, String> properties = new LinkedHashMap<String, String>();

        if (StringUtils.isBlank(idPrefix)) {
            idPrefix = DEFAULT_MENU_ITEM_PREFIX;
        }

        final Document document = getMenuDocument(logicalPath);

        // Make the root element of the menu the one with the menu identifier
        // allowing for different decorations of menu
        Element rootElement = XmlUtils.findFirstElement("//*[@id='_menu']",
                document.getFirstChild());
        if (rootElement == null) {
            final Element rootMenu = new XmlElementBuilder("menu:menu",
                    document).addAttribute("id", "_menu").build();
            rootMenu.setAttribute("z",
                    XmlRoundTripUtils.calculateUniqueKeyFor(rootMenu));
            rootElement = (Element) document.getDocumentElement().appendChild(
                    rootMenu);
        }

        // Check for existence of menu category by looking for the identifier
        // provided
        final String lcMenuCategoryName = menuCategoryName.getSymbolName()
                .toLowerCase();

        Element category = XmlUtils.findFirstElement("//*[@id='c_"
                + lcMenuCategoryName + "']", rootElement);
        // If not exists, create new one
        if (category == null) {
            category = (Element) rootElement.appendChild(new XmlElementBuilder(
                    "menu:category", document).addAttribute("id",
                    "c_" + lcMenuCategoryName).build());
            category.setAttribute("z",
                    XmlRoundTripUtils.calculateUniqueKeyFor(category));
            properties.put("menu_category_" + lcMenuCategoryName + "_label",
                    menuCategoryName.getReadableSymbolName());
        }

        // Check for existence of menu item by looking for the identifier
        // provided
        Element menuItem = XmlUtils.findFirstElement("//*[@id='" + idPrefix
                + lcMenuCategoryName + "_"
                + menuItemId.getSymbolName().toLowerCase() + "']", rootElement);
        if (menuItem == null) {
            menuItem = new XmlElementBuilder("menu:item", document)
                    .addAttribute(
                            "id",
                            idPrefix + lcMenuCategoryName + "_"
                                    + menuItemId.getSymbolName().toLowerCase())
                    .addAttribute("messageCode", globalMessageCode)
                    .addAttribute("url", link).build();
            menuItem.setAttribute("z",
                    XmlRoundTripUtils.calculateUniqueKeyFor(menuItem));
            category.appendChild(menuItem);
        }
        if (writeProps) {
            properties.put("menu_item_" + lcMenuCategoryName + "_"
                    + menuItemId.getSymbolName().toLowerCase() + "_label",
                    menuItemLabel);
            getPropFileOperations().addProperties(getProjectOperations()
                    .getPathResolver().getFocusedPath(Path.SRC_MAIN_WEBAPP),
                    "WEB-INF/i18n/application.properties", properties, true,
                    false);
        }
        getXmlRoundTripFileManager().writeToDiskIfNecessary(
                getMenuFileName(logicalPath), document);
    }

    public void addMenuItem(final JavaSymbolName menuCategoryName,
            final JavaSymbolName menuItemId, final String menuItemLabel,
            final String globalMessageCode, final String link,
            final String idPrefix, final LogicalPath logicalPath) {
        addMenuItem(menuCategoryName, menuItemId, menuItemLabel,
                globalMessageCode, link, idPrefix, true, logicalPath);
    }

    public void cleanUpFinderMenuItems(final JavaSymbolName menuCategoryName,
            final List<String> allowedFinderMenuIds,
            final LogicalPath logicalPath) {
        Validate.notNull(menuCategoryName, "Menu category identifier required");
        Validate.notNull(allowedFinderMenuIds,
                "List of allowed menu items required");

        final Document document = getMenuDocument(logicalPath);

        // Find any menu items under this category which have an id that starts
        // with the menuItemIdPrefix
        final List<Element> elements = XmlUtils.findElements(
                "//category[@id='c_"
                        + menuCategoryName.getSymbolName().toLowerCase()
                        + "']//item[starts-with(@id, '"
                        + FINDER_MENU_ITEM_PREFIX + "')]",
                document.getDocumentElement());
        if (elements.isEmpty()) {
            return;
        }
        for (final Element element : elements) {
            if (!allowedFinderMenuIds.contains(element.getAttribute("id"))
                    && isNotUserManaged(element)) {
                element.getParentNode().removeChild(element);
            }
        }
        getXmlRoundTripFileManager().writeToDiskIfNecessary(
                getMenuFileName(logicalPath), document);
    }

    /**
     * Attempts to locate a menu item and remove it.
     * 
     * @param menuCategoryName the identifier for the menu category (required)
     * @param menuItemName the menu item identifier (required)
     * @param idPrefix the prefix to be used for this menu item (optional,
     *            MenuOperations.DEFAULT_MENU_ITEM_PREFIX is default)
     */
    public void cleanUpMenuItem(final JavaSymbolName menuCategoryName,
            final JavaSymbolName menuItemName, String idPrefix,
            final LogicalPath logicalPath) {
        Validate.notNull(menuCategoryName, "Menu category identifier required");
        Validate.notNull(menuItemName, "Menu item id required");

        if (StringUtils.isBlank(idPrefix)) {
            idPrefix = DEFAULT_MENU_ITEM_PREFIX;
        }

        final Document document = getMenuDocument(logicalPath);

        // Find menu item under this category if exists
        final Element element = XmlUtils.findFirstElement("//category[@id='c_"
                + menuCategoryName.getSymbolName().toLowerCase()
                + "']//item[@id='" + idPrefix
                + menuCategoryName.getSymbolName().toLowerCase() + "_"
                + menuItemName.getSymbolName().toLowerCase() + "']",
                document.getDocumentElement());
        if (element == null) {
            return;
        }
        if (isNotUserManaged(element)) {
            element.getParentNode().removeChild(element);
        }

        getXmlRoundTripFileManager().writeToDiskIfNecessary(
                getMenuFileName(logicalPath), document);
    }

    private Document getMenuDocument(final LogicalPath logicalPath) {
        try {
            return XmlUtils.readXml(getMenuFileInputStream(logicalPath));
        }
        catch (final Exception e) {
            throw new IllegalArgumentException("Unable to parse menu.jspx"
                    + (StringUtils.isBlank(e.getMessage()) ? "" : " ("
                            + e.getMessage() + ")"), e);
        }
    }

    private InputStream getMenuFileInputStream(final LogicalPath logicalPath) {
        final String menuFileName = getMenuFileName(logicalPath);
        if (!getFileManager().exists(menuFileName)) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(), "menu.jspx");
                outputStream = getFileManager().createFile(menuFileName)
                        .getOutputStream();
                IOUtils.copy(inputStream, outputStream);
            }
            catch (final IOException e) {
                throw new IllegalStateException(
                        "Encountered an error during copying of menu.jspx for MVC Menu addon.",
                        e);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }

        final PathResolver pathResolver = getProjectOperations().getPathResolver();

        final String menuPath = pathResolver.getIdentifier(logicalPath,
                "WEB-INF/tags/menu/menu.tagx");
        if (!getFileManager().exists(menuPath)) {
            InputStream inputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(), "menu.tagx");
                getFileManager().createOrUpdateTextFileIfRequired(menuPath,
                        IOUtils.toString(inputStream), false);
            }
            catch (final Exception e) {
                throw new IllegalStateException(
                        "Encountered an error during copying of menu.tagx for MVC Menu addon.",
                        e);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
            }
        }

        final String itemPath = pathResolver.getIdentifier(logicalPath,
                "WEB-INF/tags/menu/item.tagx");
        if (!getFileManager().exists(itemPath)) {
            InputStream inputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(), "item.tagx");
                getFileManager().createOrUpdateTextFileIfRequired(menuPath,
                        IOUtils.toString(inputStream), false);
            }
            catch (final Exception e) {
                throw new IllegalStateException(
                        "Encountered an error during copying of item.tagx for MVC Menu addon.",
                        e);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
            }
        }

        final String categoryPath = pathResolver.getIdentifier(logicalPath,
                "WEB-INF/tags/menu/category.tagx");
        if (!getFileManager().exists(categoryPath)) {
            InputStream inputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(),
                        "category.tagx");
                getFileManager().createOrUpdateTextFileIfRequired(menuPath,
                        IOUtils.toString(inputStream), false);
            }
            catch (final Exception e) {
                throw new IllegalStateException(
                        "Encountered an error during copying of category.tagx for MVC Menu addon.",
                        e);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
            }
        }

        return getFileManager().getInputStream(menuFileName);
    }

    private String getMenuFileName(final LogicalPath logicalPath) {
        return getProjectOperations().getPathResolver().getIdentifier(logicalPath,
                "WEB-INF/views/menu.jspx");
    }

    private boolean isNotUserManaged(final Element element) {
        return "?".equals(element.getAttribute("z"))
                || XmlRoundTripUtils.calculateUniqueKeyFor(element).equals(
                        element.getAttribute("z"));
    }
    
    public FileManager getFileManager(){
    	if(fileManager == null){
    		// Get all Services implement FileManager interface
    		try {
    			ServiceReference<?>[] references = this.context.getAllServiceReferences(FileManager.class.getName(), null);
    			
    			for(ServiceReference<?> ref : references){
    				return (FileManager) this.context.getService(ref);
    			}
    			
    			return null;
    			
    		} catch (InvalidSyntaxException e) {
    			LOGGER.warning("Cannot load FileManager on MenuOperationsImpl.");
    			return null;
    		}
    	}else{
    		return fileManager;
    	}
    }
    
    public ProjectOperations getProjectOperations(){
    	if(projectOperations == null){
    		// Get all Services implement ProjectOperations interface
    		try {
    			ServiceReference<?>[] references = this.context.getAllServiceReferences(ProjectOperations.class.getName(), null);
    			
    			for(ServiceReference<?> ref : references){
    				return (ProjectOperations) this.context.getService(ref);
    			}
    			
    			return null;
    			
    		} catch (InvalidSyntaxException e) {
    			LOGGER.warning("Cannot load ProjectOperations on MenuOperationsImpl.");
    			return null;
    		}
    	}else{
    		return projectOperations;
    	}
    }
    
    public PropFileOperations getPropFileOperations(){
    	if(propFileOperations == null){
    		// Get all Services implement PropFileOperations interface
    		try {
    			ServiceReference<?>[] references = this.context.getAllServiceReferences(PropFileOperations.class.getName(), null);
    			
    			for(ServiceReference<?> ref : references){
    				return (PropFileOperations) this.context.getService(ref);
    			}
    			
    			return null;
    			
    		} catch (InvalidSyntaxException e) {
    			LOGGER.warning("Cannot load PropFileOperations on MenuOperationsImpl.");
    			return null;
    		}
    	}else{
    		return propFileOperations;
    	}
    }
    
    public XmlRoundTripFileManager getXmlRoundTripFileManager(){
    	if(xmlRoundTripFileManager == null){
    		// Get all Services implement XmlRoundTripFileManager interface
    		try {
    			ServiceReference<?>[] references = this.context.getAllServiceReferences(XmlRoundTripFileManager.class.getName(), null);
    			
    			for(ServiceReference<?> ref : references){
    				return (XmlRoundTripFileManager) this.context.getService(ref);
    			}
    			
    			return null;
    			
    		} catch (InvalidSyntaxException e) {
    			LOGGER.warning("Cannot load XmlRoundTripFileManager on MenuOperationsImpl.");
    			return null;
    		}
    	}else{
    		return xmlRoundTripFileManager;
    	}
    }
}