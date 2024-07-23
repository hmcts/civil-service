package uk.gov.hmcts.reform.civil.service.documentmerge;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.jaxb.Context;
import org.docx4j.jaxb.XPathBinderAssociationIsPartialException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Color;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentMergingService {

    static String SECTION_START_FORMAT = "<<<<<%s>>>>";

    public byte[] merge(List<MergeDoc> docs) {
        if (docs == null || docs.isEmpty()) {
            throw new IllegalArgumentException("The provided document list is null or empty.");
        }

        byte[] baseDoc = docs.get(0).getFile();

        try (InputStream baseDocStream = new ByteArrayInputStream(baseDoc);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            out.write(baseDocStream.readAllBytes());

            for (int i = 1; i < docs.size(); i++) {
                try (InputStream contentStream = new ByteArrayInputStream(docs.get(i).getFile());
                     ByteArrayOutputStream tempOut = new ByteArrayOutputStream()) {
                    tempOut.write(out.toByteArray());
                    out.reset();

                    mergeContentStream(tempOut, contentStream,
                            String.format(SECTION_START_FORMAT, docs.get(i).getSectionHeader()));

                    out.write(tempOut.toByteArray()); // Write merged content back to out
                } catch (IOException | Docx4JException | JAXBException e) {
                    log.error("Error merging document at index {}: {}. Skipping document.", i, e.getMessage(), e);
                }
            }
            return out.toByteArray();

        } catch (IOException e) {
            log.error("There was a problem merging the base document: {}. Returning base document only.", e.getMessage(), e);
        }

        return docs.get(0).getFile();
    }

    private ByteArrayOutputStream mergeContentStream(ByteArrayOutputStream baseDocumentStream, InputStream contentStream,
                                                     String prependText) throws IOException, Docx4JException, JAXBException {
        try (ByteArrayInputStream baseContentStream = new ByteArrayInputStream(baseDocumentStream.toByteArray())) {
            WordprocessingMLPackage contentDoc = WordprocessingMLPackage.load(contentStream);

            contentDoc.getMainDocumentPart().getContent().add(0, createHeading(prependText, 1));
            contentDoc.getMainDocumentPart().getContent().add(createParagraph("=================================================================================="));
            setAllTextColour(contentDoc, "FF0000");

            WordprocessingMLPackage baseDoc = WordprocessingMLPackage.load(baseContentStream);
            WordprocessingMLPackage mergedDoc = combineDocuments(baseDoc, contentDoc);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            mergedDoc.save(outputStream);
            return outputStream;
        }
    }

    private WordprocessingMLPackage combineDocuments(WordprocessingMLPackage baseDoc, WordprocessingMLPackage contentDoc)
            throws JAXBException, XPathBinderAssociationIsPartialException {
        for (Object content : contentDoc.getMainDocumentPart().getJAXBNodesViaXPath("//w:body", false)) {
            for (Object contentBody : ((org.docx4j.wml.Body) content).getContent()) {
                baseDoc.getMainDocumentPart().addObject(contentBody);
            }
        }
        return baseDoc;
    }

    private P createHeading(String headingText, int headingLevel) {
        ObjectFactory factory = Context.getWmlObjectFactory();
        P paragraph = factory.createP();

        PPr ppr = factory.createPPr();
        paragraph.setPPr(ppr);

        var style = factory.createPPrBasePStyle();
        style.setVal("Heading" + headingLevel);
        ppr.setPStyle(style);

        R run = factory.createR();
        Text text = factory.createText();
        text.setValue(headingText);

        run.getContent().add(text);
        paragraph.getContent().add(run);

        return paragraph;
    }

    private P createParagraph(String content) {
        ObjectFactory factory = Context.getWmlObjectFactory();
        P paragraph = factory.createP();

        PPr ppr = factory.createPPr();
        paragraph.setPPr(ppr);

        R run = factory.createR();
        Text text = factory.createText();
        text.setValue(content);

        run.getContent().add(text);
        paragraph.getContent().add(run);

        return paragraph;
    }

    public static void setAllTextColour(WordprocessingMLPackage wordMLPackage, String colourCode) {
        MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();
        List<Object> paragraphs = mainDocumentPart.getContent();

        for (Object paragraph : paragraphs) {
            if (paragraph instanceof JAXBElement) {
                paragraph = ((JAXBElement<?>) paragraph).getValue();
            }
            if (paragraph instanceof P) {
                List<Object> texts = ((P) paragraph).getContent();
                for (Object textElement : texts) {
                    if (textElement instanceof JAXBElement) {
                        textElement = ((JAXBElement<?>) textElement).getValue();
                    }
                    if (textElement instanceof R) {
                        List<Object> contents = ((R) textElement).getContent();
                        for (Object content : contents) {
                            if (content instanceof JAXBElement) {
                                content = ((JAXBElement<?>) content).getValue();
                            }
                            if (content instanceof Text) {
                                RPr rpr = ((R) textElement).getRPr();
                                if (rpr == null) {
                                    rpr = new RPr();
                                    ((R) textElement).setRPr(rpr);
                                }
                                Color color = new Color();
                                color.setVal(colourCode);
                                rpr.setColor(color);
                            }
                        }
                    }
                }
            }
        }
    }
}
