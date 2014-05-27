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
      TreeElement root = new TreeElement(info.getClassName());

      // Descriptor
      TableElement descriptor = new TableElement().
          overflow(Overflow.HIDDEN).
          rightCellPadding(1);
      Descriptor descriptorInfo = info.getDescriptor();
      if (descriptorInfo != null) {
        for (String fieldName : descriptorInfo.getFieldNames()) {
          String fieldValue = String.valueOf(descriptorInfo.getFieldValue(fieldName));
          descriptor.row(fieldName, fieldValue);
        }
      }

      // Attributes
      TableElement attributes = new TableElement().
          overflow(Overflow.HIDDEN).
          rightCellPadding(1).
          add(new RowElement().style(Decoration.bold.fg(Color.black).bg(Color.white)).add("NAME", "TYPE", "DESCRIPTION"));
      for (MBeanAttributeInfo attributeInfo : info.getAttributes()) {
        attributes.row(attributeInfo.getName(), attributeInfo.getType(), attributeInfo.getDescription());
      }

      // Operations
      TreeElement operations = new TreeElement("Operations");
      for (MBeanOperationInfo operationInfo : info.getOperations()) {
        TableElement signature = new TableElement().
            overflow(Overflow.HIDDEN).
            rightCellPadding(1);
        MBeanParameterInfo[] parameterInfos = operationInfo.getSignature();
        for (MBeanParameterInfo parameterInfo : parameterInfos) {
          signature.row(parameterInfo.getName(), parameterInfo.getType(), parameterInfo.getDescription());
        }
        TreeElement operation = new TreeElement(operationInfo.getName());
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
                new RowElement().add("Type: ", operationInfo.getReturnType()),
                new RowElement().add("Description: ", operationInfo.getDescription()),
                new RowElement().add("Impact: ", impact),
                new RowElement().add(new LabelElement("Signature: "), signature)
            )
        );

        operations.addChild(operation);
      }

      //
      root.addChild(
        new TableElement().leftCellPadding(1).overflow(Overflow.HIDDEN).
          row("ClassName", info.getClassName()).
          row("Description", info.getDescription()
        )
      );
      root.addChild(new TreeElement("Descriptor").addChild(descriptor));
      root.addChild(new TreeElement("Attributes").addChild(attributes));
      root.addChild(operations);

      //
      renderers.add(root.renderer());
    }




    return LineRenderer.vertical(renderers);
  }
}
