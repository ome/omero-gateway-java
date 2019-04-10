package omero.gateway.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import omero.gateway.model.DataObject;
import omero.model.IObject;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * Some helper methods dealing with XXXYYYLinkI IObjects, e.g.
 * ImageAnnotationLinkI
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 *
 */
public class Links {

    private Links() {
    }

    /**
     * Get the Link class for a certain child parent combination, e.g. parent:
     * DatasetData, child: ImageData => omero.model.DatasetImageLinkI
     *
     * @param parent
     *            The parent
     * @param child
     *            The child
     * @return See above
     * @throws ClassNotFoundException
     */
    public static Class<? extends IObject> getLinkClass(
            Class<? extends DataObject> parent,
            Class<? extends DataObject> child) throws ClassNotFoundException {
        String parentType = getType(parent);
        String childType = getType(child);
        String type = parentType + childType + "LinkI";
        return (Class<? extends IObject>) Class.forName("omero.model." + type);
    }

    /**
     * Set the parent, child or both of an existing link
     *
     * @param link   The link object
     * @param parent The parent (or <code>null</code>)
     * @param child  The child (or <code>null</code>)
     * @return The link object
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public static IObject setObjects(IObject link, DataObject parent,
                                     DataObject child) throws
            NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        Class<? extends IObject> clazz = link.getClass();
        if (parent != null) {
            PropertyUtils.setProperty(link, "parent", parent.asIObject());
        }
        if (child != null) {
            PropertyUtils.setProperty(link, "child", child.asIObject());
            return link;
        }

    /**
     * Get the IObject type of DataObject class with respect to links
     *
     * @param clazz
     *            The DataObject class
     * @return See above
     */
    private static String getType(Class<? extends DataObject> clazz) {
        String type = clazz.getSimpleName();
        if (type.endsWith("Data"))
            type = type.substring(0, type.length() - 4);
        if (type.contains("Annotation"))
            type = "Annotation";
        return type;
    }
}