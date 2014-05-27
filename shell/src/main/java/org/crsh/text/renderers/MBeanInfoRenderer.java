package org.crsh.text.renderers;

import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.LineRenderer;
import org.crsh.text.Renderer;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.Overflow;
import org.crsh.text.ui.RowElement;
import org.crsh.text.ui.TableElement;
import org.crsh.text.ui.TreeElement;

import javax.management.Descriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Julien Viet
 */
public class MBeanInfoRenderer extends Renderer<MBeanInfo> {

  @Override
  public Class<MBeanInfo> getType() {
    return MBeanInfo.class;
  }

  @Override
  public LineRenderer renderer(Iterator<MBeanInfo> stream) {

    List<LineRenderer> renderers = new ArrayList<LineRenderer>();

    while (stream.hasNext()) {
      MBeanInfo info = stream.next();

      //
      TreeElement root = new TreeElement(new LabelElement(info.getClassName()));

      // Descriptor
      TableElement descriptor = new TableElement().
          overflow(Overflow.HIDDEN).
          rightCellPadding(1);
      Descriptor descriptorInfo = info.getDescriptor();
      if (descriptorInfo != null) {
        for (String fieldName : descriptorInfo.getFieldNames()) {
          String fieldValue = String.valueOf(descriptorInfo.getFieldValue(fieldName));
          descriptor.add(new RowElement().add(
              new LabelElement(fieldName),
              new LabelElement(fieldValue)
          ));
        }
      }

      // Attributes
      TableElement attributes = new TableElement().
          overflow(Overflow.HIDDEN).
          rightCellPadding(1).
          add(new RowElement().style(Decoration.bold.fg(Color.black).bg(Color.white)).add(
              new LabelElement("NAME"),
              new LabelElement("TYPE"),
              new LabelElement("DESCRIPTION")
          ));
      for (MBeanAttributeInfo attributeInfo : info.getAttributes()) {
        attributes.add(new RowElement().add(
            new LabelElement(attributeInfo.getName()),
            new LabelElement(attributeInfo.getType()),
            new LabelElement(attributeInfo.getDescription())
        ));
      }

      // Operations
      TreeElement operations = new TreeElement(new LabelElement("Operations"));
      for (MBeanOperationInfo operationInfo : info.getOperations()) {
        TableElement signature = new TableElement().
            overflow(Overflow.HIDDEN).
            rightCellPadding(1);
        MBeanParameterInfo[] parameterInfos = operationInfo.getSignature();
        for (MBeanParameterInfo parameterInfo : parameterInfos) {
          signature.add(new RowElement().add(
              new LabelElement(parameterInfo.getName()),
              new LabelElement(parameterInfo.getType()),
              new LabelElement(parameterInfo.getDescription())
          ));
        }
        TreeElement operation = new TreeElement(new LabelElement(operationInfo.getName()));
        String impact;
        switch (operationInfo.getImpact()) {
          case MBeanOperationInfo.ACTION:
            impact = "ACTION";
            break;
          case MBeanOperationInfo.INFO:
            impact = "INFO";
            break;
          case MBeanOperationInfo.ACTION_INFO:
            impact = "ACTION_INFO";
            break;
          default:
            impact = "UNKNOWN";
        }
        operation.addChild(new TableElement().
            add(
                new RowElement().add(new LabelElement("Type: "), new LabelElement(operationInfo.getReturnType())),
                new RowElement().add(new LabelElement("Description: "), new LabelElement(operationInfo.getDescription())),
                new RowElement().add(new LabelElement("Impact: "), new LabelElement(impact)),
                new RowElement().add(new LabelElement("Signature: "), signature)
            )
        );

        operations.addChild(operation);
      }

      //
      root.addChild(new TableElement().leftCellPadding(1).overflow(Overflow.HIDDEN).add(
          new RowElement().add(new LabelElement("ClassName"), new LabelElement(info.getClassName())),
          new RowElement().add(new LabelElement("Description"), new LabelElement(info.getDescription()))
      ));
      root.addChild(new TreeElement(new LabelElement("Descriptor")).addChild(descriptor));
      root.addChild(new TreeElement(new LabelElement("Attributes")).addChild(attributes));
      root.addChild(operations);

      //
      renderers.add(root.renderer());
    }




    return LineRenderer.vertical(renderers);
  }
}
