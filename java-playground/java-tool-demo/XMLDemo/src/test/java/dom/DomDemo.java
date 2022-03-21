package dom;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author dominiczhu
 * @version 1.0
 * @title DomDemo
 * @date 2021/3/22 下午5:18
 */
public class DomDemo {

    @Test
    public void readXml() throws DocumentException {
        Document document = getDoc();

        Element root = document.getRootElement();
        System.out.println(root.nodeCount());

        Iterator<Attribute> attributeIterator = root.attributeIterator();
        while (attributeIterator.hasNext()) {
            Attribute attr = attributeIterator.next();
            System.out.println("name: " + attr.getName() + "\tvalue: " + attr.getValue());
        }

        for (Iterator<Element> it = root.elementIterator(); it.hasNext(); ) {
            Element element = it.next();
            System.out.println(element.attribute(0));
            // do something
        }


        System.out.println();

    }

    private Document getDoc() throws DocumentException {
        String filePath = "src/main/resources/city.xml";
        File f = new File(filePath);
        SAXReader reader = new SAXReader();
        return reader.read(f);
    }

    @Test
    public void findDescendants() throws DocumentException {
        Document document = getDoc();

        Set<String> cityNames = new HashSet<>();
        cityNames.add("辽宁省");

        Set<Element> descendants = new HashSet<>();
        findTargetNodes(document.getRootElement(), descendants, cityNames, false);
        System.out.println(System.currentTimeMillis());
        for (Element e : descendants) {
            System.out.println("cname: " + e.attribute("cname").getValue() + "\tgb_code: " + e.attribute("gb_code").getValue());
        }
        System.out.println(System.currentTimeMillis());

    }

    private void findTargetNodes(Element node, Set<Element> descendants, Set<String> cityNames, Boolean isDescendants) {
        // dfs
        if (isDescendants)
            descendants.add(node);

        Iterator<Element> elementIterator = node.elementIterator();

        while (elementIterator.hasNext()) {
            Element child = elementIterator.next();
            if (isDescendants || cityNames.contains(child.attribute("cname").getValue())) {
                findTargetNodes(child, descendants, cityNames, true);
            } else {
                findTargetNodes(child, descendants, cityNames, false);
            }
        }
    }

}
