/*
 * JDiff - HTML report of API differences
 * Copyright (C) 2021  José Roberto de Araújo Júnior <joserobjr@powernukkit.org>
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>.
 */

package jdiff;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

/**
 * Creates an API object from an XML file. The API object is the internal
 * representation of an API.
 * All methods in this class for populating an API object are static.
 *
 * @author Matthew Doar, mdoar@pobox.com
 */
public class XMLToAPI {

    /**
     * If set, then store and display the whole qualified name of exceptions.
     * If not set, then store and display just the name of the exception,
     * which is shorter, but may not detect when an exception changes class,
     * but retains the same name.
     */
    private static final boolean showExceptionTypes = true;

    /**
     * If set, validate the XML which represents an API. By default, this is
     * not set for reasons of efficiency, and also because if JDiff generated
     * the XML, it should not need validating.
     */
    public static boolean validateXML;

    /**
     * The instance of the API object which is populated from the file.
     */
    private static API api_;

    /**
     * Default constructor.
     */
    private XMLToAPI() {
    }

    /**
     * Read the file where the XML representing the API is stored.
     *
     * @param filename             The full name of the file containing the XML
     *                             representing the API
     * @param createGlobalComments If set, then store possible comments
     * @param apiName              The simple name of the API file. If -oldapidir and
     *                             -newapidir are not used, then this is the same as
     *                             the filename parameter
     */
    public static API readFile(String filename, boolean createGlobalComments,
                               String apiName) {
        // The instance of the API object which is populated from the file.
        api_ = new API();
        api_.name_ = apiName; // Checked later
        try {
            XMLReader parser = null;
            DefaultHandler handler = new APIHandler(api_, createGlobalComments);
            try {
                String parserName = System.getProperty("org.xml.sax.driver");
                if (parserName == null) {
                    parser = org.xml.sax.helpers.XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
                } else {
                    // Let the underlying mechanisms try to work out which
                    // class to instantiate
                    parser = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
                }
            } catch (SAXException saxe) {
                System.out.println("SAXException: " + saxe);
                saxe.printStackTrace();
                System.exit(1);
            }
            if (validateXML) {
                parser.setFeature("http://xml.org/sax/features/namespaces", true);
                parser.setFeature("http://xml.org/sax/features/validation", true);
                parser.setFeature("http://apache.org/xml/features/validation/schema", true);
            }

            parser.setContentHandler(handler);
            parser.setErrorHandler(handler);
            try (FileInputStream fis = new FileInputStream(filename)) {
                parser.parse(new InputSource(new BufferedInputStream(fis)));
            }
        } catch (org.xml.sax.SAXNotRecognizedException snre) {
            System.out.println("SAX Parser does not recognize feature: " + snre);
            snre.printStackTrace();
            System.exit(1);
        } catch (org.xml.sax.SAXNotSupportedException snse) {
            System.out.println("SAX Parser feature is not supported: " + snse);
            snse.printStackTrace();
            System.exit(1);
        } catch (org.xml.sax.SAXException saxe) {
            System.out.println("SAX Exception parsing file '" + filename + "' : " + saxe);
            saxe.printStackTrace();
            System.exit(1);
        } catch (java.io.IOException ioe) {
            System.out.println("IOException parsing file '" + filename + "' : " + ioe);
            ioe.printStackTrace();
            System.exit(1);
        }

        // Add the inherited methods and fields to each class
        addInheritedElements();
        return api_;
    } //readFile()

    /**
     * Add the inherited methods and fields to each class in turn.
     */
    public static void addInheritedElements() {
        for (PackageAPI pkg : api_.packages_) {
            for (ClassAPI cls : pkg.classes_) {
                // Look up any inherited classes or interfaces
                if (cls.extends_ != null) {
                    ClassAPI parent = api_.classes_.get(cls.extends_);
                    if (parent != null)
                        addInheritedElements(cls, parent, cls.extends_);
                }
                if (cls.implements_.size() != 0) {
                    for (String implName : cls.implements_) {
                        ClassAPI parent = api_.classes_.get(implName);
                        if (parent != null)
                            addInheritedElements(cls, parent, implName);
                    }
                }
            } //while (iter2.hasNext())
        } //while (iter.hasNext())
    }

    /**
     * Add all the inherited methods and fields in the second class to
     * the first class, marking them as inherited from the second class.
     * Do not add a method or a field if it is already defined locally.
     * <p>
     * Only elements at the specified visibility level or
     * higher appear in the XML file. All that remains to be tested for
     * a private element, which is never inherited.
     * <p>
     * If the parent class inherits any classes or interfaces, call this
     * method recursively with those parents.
     */
    public static void addInheritedElements(ClassAPI child, ClassAPI parent,
                                            String fqParentName) {
        if (parent.methods_.size() != 0) {
            for (MethodAPI m : parent.methods_) {
                // See if it the method is overridden locally
                boolean overridden = false;
                for (MethodAPI localM : child.methods_) {
                    if (localM.name_.compareTo(m.name_) == 0 &&
                            localM.getSignature().compareTo(m.getSignature()) == 0)
                        overridden = true;
                }
                if (!overridden && m.inheritedFrom_ == null &&
                        m.modifiers_.visibility != null &&
                        m.modifiers_.visibility.compareTo("private") != 0) {
                    MethodAPI m2 = new MethodAPI(m);
                    m2.inheritedFrom_ = fqParentName;
                    child.methods_.add(m2);
                }
            }
        }
        if (parent.fields_.size() != 0) {
            for (FieldAPI f : parent.fields_) {
                if (!child.fields_.contains(f) &&
                        f.inheritedFrom_ == null &&
                        f.modifiers_.visibility != null &&
                        f.modifiers_.visibility.compareTo("private") != 0) {
                    FieldAPI f2 = new FieldAPI(f);
                    f2.inheritedFrom_ = fqParentName;
                    child.fields_.add(f2);
                }
            }
        }

        // Look up any inherited classes or interfaces
        if (parent.extends_ != null) {
            ClassAPI parent2 = api_.classes_.get(parent.extends_);
            if (parent2 != null)
                addInheritedElements(child, parent2, parent.extends_);
        }
        if (parent.implements_.size() != 0) {
            for (String implName : parent.implements_) {
                ClassAPI parent2 = api_.classes_.get(implName);
                if (parent2 != null)
                    addInheritedElements(child, parent2, implName);
            }
        }
    }

    /**
     * Set the name of the API object.
     *
     * @param name The name of the package.
     */
    public static void nameAPI(String name) {
        if (name == null) {
            System.out.println("Error: no API identifier found in the XML file '" + api_.name_ + "'");
            System.exit(3);
        }
        // Check the given name against the filename currently stored in
        // the name_ field
        String filename2 = name.replace(' ', '_');
        filename2 += ".xml";
        if (filename2.compareTo(api_.name_) != 0) {
            System.out.println("Warning: API identifier in the XML file (" +
                    name + ") differs from the name of the file '" +
                    api_.name_ + "'");
        }
        api_.name_ = name;
    }

    /**
     * Create a new package and add it to the API. Called by the XML parser.
     *
     * @param name The name of the package.
     */
    public static void addPackage(String name) {
        api_.currPkg_ = new PackageAPI(name);
        api_.packages_.add(api_.currPkg_);
    }

    /**
     * Create a new class and add it to the current package. Called by the XML parser.
     *
     * @param name      The name of the class.
     * @param parent    The name of the parent class, null if no class is extended.
     * @param modifiers Modifiers for this class.
     */
    public static void addClass(String name, String parent,
                                boolean isAbstract,
                                Modifiers modifiers) {
        api_.currClass_ = new ClassAPI(name, parent, false, isAbstract, modifiers);
        api_.currPkg_.classes_.add(api_.currClass_);
        String fqName = api_.currPkg_.name_ + "." + name;
        ClassAPI caOld = api_.classes_.put(fqName, api_.currClass_);
        if (caOld != null) {
            System.out.println("Warning: duplicate class : " + fqName + " found. Using the first instance only.");
        }
    }

    /**
     * Add an new interface and add it to the current package. Called by the
     * XML parser.
     *
     * @param name   The name of the interface.
     * @param parent The name of the parent interface, null if no
     *               interface is extended.
     */
    public static void addInterface(String name, String parent,
                                    boolean isAbstract,
                                    Modifiers modifiers) {
        api_.currClass_ = new ClassAPI(name, parent, true, isAbstract, modifiers);
        api_.currPkg_.classes_.add(api_.currClass_);
    }

    /**
     * Add an inherited interface to the current class. Called by the XML
     * parser.
     *
     * @param name The name of the inherited interface.
     */
    public static void addImplements(String name) {
        api_.currClass_.implements_.add(name);
    }

    /**
     * Add a constructor to the current class. Called by the XML parser.
     *
     * @param type      The type of the constructor.
     * @param modifiers Modifiers for this constructor.
     */
    public static void addCtor(String type, Modifiers modifiers) {
        String t = type;
        if (t == null)
            t = "void";
        api_.currCtor_ = new ConstructorAPI(t, modifiers);
        api_.currClass_.ctors_.add(api_.currCtor_);
    }

    /**
     * Add a method to the current class. Called by the XML parser.
     *
     * @param name       The name of the method.
     * @param returnType The return type of the method, null if it is void.
     * @param modifiers  Modifiers for this method.
     */
    public static void addMethod(String name, String returnType,
                                 boolean isAbstract, boolean isNative,
                                 boolean isSynchronized, Modifiers modifiers) {
        String rt = returnType;
        if (rt == null)
            rt = "void";
        api_.currMethod_ = new MethodAPI(name, rt, isAbstract, isNative,
                isSynchronized, modifiers);
        api_.currClass_.methods_.add(api_.currMethod_);
    }

    /**
     * Add a field to the current class. Called by the XML parser.
     *
     * @param name      The name of the field.
     * @param type      The type of the field, null if it is void.
     * @param modifiers Modifiers for this field.
     */
    public static void addField(String name, String type, boolean isTransient,
                                boolean isVolatile, String value, Modifiers modifiers) {
        String t = type;
        if (t == null)
            t = "void";
        api_.currField_ = new FieldAPI(name, t, isTransient, isVolatile, value, modifiers);
        api_.currClass_.fields_.add(api_.currField_);
    }

    /**
     * Add a parameter to the current method. Called by the XML parser.
     * Constuctors have their type (signature) in an attribute, since it
     * is often shorter and makes parsing a little easier.
     *
     * @param name The name of the parameter.
     * @param type The type of the parameter, null if it is void.
     */
    public static void addParam(String name, String type) {
        String t = type;
        if (t == null)
            t = "void";
        ParamAPI paramAPI = new ParamAPI(name, t);
        api_.currMethod_.params_.add(paramAPI);
    }

    /**
     * Add an exception to the current method or constructor.
     * Called by the XML parser.
     *
     * @param name        The name of the parameter.
     * @param type        The type of the parameter.
     *                    May be null in JDiff1.0.8 and earlier versions.
     * @param currElement Name of the current element.
     */
    public static void addException(String name, String type, String currElement) {
        String exceptionId = type;
        if (type == null || !showExceptionTypes)
            exceptionId = name;
        if (currElement.compareTo("method") == 0) {
            if (api_.currMethod_.exceptions_.compareTo("no exceptions") == 0)
                api_.currMethod_.exceptions_ = exceptionId;
            else
                api_.currMethod_.exceptions_ += ", " + exceptionId;
        } else {
            if (api_.currCtor_.exceptions_.compareTo("no exceptions") == 0)
                api_.currCtor_.exceptions_ = exceptionId;
            else
                api_.currCtor_.exceptions_ += ", " + exceptionId;
        }
    }
}  
