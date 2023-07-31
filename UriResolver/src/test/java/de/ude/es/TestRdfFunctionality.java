package de.ude.es;

import de.ude.es.protocolabstraction.rdfconversion.RDFObjectGetter;
import de.ude.es.protocolabstraction.rdfconversion.RDFObjectGetterImpl;
import org.apache.jena.base.Sys;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.BagImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertTrue;
@Disabled
class TestRdfFunctionality {
    String namespace = "http://test/";
    @Test
    void testLiteralWithTwoProperties(){

        Model model = ModelFactory.createDefaultModel();

        Property hasFunctions = model.createProperty(namespace,"hasFunctions");
        Property callFunction = model.createProperty(namespace,"callFunction");

        Resource RemoteControl = model.createResource(namespace +"RemoteControl");

        Literal led_on = model.createLiteral("Led_On");


        RDFList list = model.createList();
        list = list.with(model.createLiteral("Button0_Pressed"));
        list.add(led_on);

        RemoteControl.addProperty(hasFunctions,list);

        model.write(System.out,"TURTLE");

        RemoteControl.addLiteral(callFunction,led_on);

        model.write(System.out,"TURTLE");
    }

    @Test
    void testCombinedModel(){

        Model model = ModelFactory.createDefaultModel();

        Resource RemoteControl = model.createResource(namespace + "RemoteControl");
        Resource projector = model.createResource(namespace + "Projector");
        Resource actionP = model.createResource(namespace + "Projector/Action");
        Resource actionR = model.createResource(namespace + "RemoteControl/Action");

        Property hasAction = model.createProperty(namespace, "hasAction");
        Property hasURI = model.createProperty(namespace, "hasURI");

        RemoteControl.addProperty(hasAction, actionR);
        projector.addProperty(hasAction, actionP);

        actionP.addLiteral(hasURI, "hallo");
        actionR.addLiteral(hasURI, "bye");

        model.write(System.out, "TURTLE");
    }

    @Test
    void testConnectEmptyNodes() {

        Model model = ModelFactory.createDefaultModel();

        Resource empty1 = model.createResource();
        Resource empty2 = model.createResource();

        Property has = model.createProperty(namespace,"has");

        empty1.addProperty(has,empty2);

        model.write(System.out, "TURTLE");
    }

    @Test
    void testConnectResource() {

        Model model = ModelFactory.createDefaultModel();

        Resource test = new ResourceImpl(namespace,"111/test");

        Resource test2 = model.createResource(namespace + "111/test");

        System.out.println(test.getNameSpace());
        System.out.println(test2.getNameSpace());
    }

    @Test
    void testEmptyVsSingleResource() {
        Model emptyModel = ModelFactory.createDefaultModel();

        Model singleResourceModel = ModelFactory.createDefaultModel();

        singleResourceModel.createResource("http://test");

        assertTrue(emptyModel.isIsomorphicWith(singleResourceModel));
    }

    @Test
    void testMappingOnEmptyNodes() {
        Model testModel = ModelFactory.createDefaultModel();

        Property has = testModel.createProperty(namespace,"has");

        Resource empty1 = testModel.createResource();
        Resource empty2 = testModel.createResource();

        empty1.addLiteral(has,"1");
        empty2.addLiteral(has,"2");
        testModel.write(System.out,"TURTLE");
    }

    @Test
    void testMappingEmptyNode2() {
        Model testModel = ModelFactory.createDefaultModel();

        Property has = testModel.createProperty(namespace,"has");

        Resource empty1 = testModel.createResource();
        Resource empty2 = testModel.createResource();

        empty1.hasProperty(has,empty2);
        empty1.addLiteral(has,"1");
        empty2.addLiteral(has,"2");
        testModel.write(System.out,"TURTLE");
    }

    @Test
    void testConnectingEmpty() {
        Model testModel = ModelFactory.createDefaultModel();

        Property has = testModel.createProperty(namespace,"has");

        Resource empty1 = testModel.createResource();
        Resource empty2 = testModel.createResource();

        empty1.addProperty(has,empty2);
        testModel.write(System.out,"TURTLE");

        Model testModel2 = ModelFactory.createDefaultModel();

        testModel2.add(empty1,has,empty1);
        testModel2.write(System.out,"TURTLE");
    }

    @Test
    void testList() {
        Model model = ModelFactory.createDefaultModel();

        Resource bot = model.createResource("NAME");
        Bag x = model.createBag();
        x.add(model.createLiteral("test"));

        Property has = model.createProperty(namespace,"has");

        model.add(bot,has,x);

        System.out.println("model-1");
        model.write(System.out,"TURTLE");

        Statement stmt = bot.listProperties().nextStatement();
        Bag bag = stmt.getBag();

        System.out.println(stmt.asTriple());

        Model resultModel = ModelFactory.createDefaultModel();

        //Bag b2 = resultModel.createBag();
        //b2.add(resultModel.createLiteral("test2"));
        bag.inModel(resultModel);

        resultModel.add(bot,has,bag);
        System.out.println("model-3");
        resultModel.write(System.out,"TURTLE");
    }

    @Test
    void testObjectGetter() {
        Model model = ModelFactory.createDefaultModel();
        Resource resource = model.createResource("NAME");

        RDFNode[] items = {model.createLiteral("test")};
        RDFList x = model.createList(items);

        Property has = model.createProperty(namespace,"has");
        model.add(resource,has,x);

        model.write(System.out,"TURTLE");

        RDFObjectGetter getter = new RDFObjectGetterImpl(namespace);
        RDFList list = getter.getRDFList(resource,has,model);

        Model result = ModelFactory.createDefaultModel();
        RDFList contentList = result.createList(list.asJavaList().toArray(new RDFNode[0]));
        result.add(resource,has,contentList);

        result.write(System.out,"TURTLE");
    }

    @Test
    void testBuildAnswer() {
        Model request = ModelFactory.createDefaultModel();
        Resource subjectRequest = request.createResource("MESSAGEBASE");
        Property has = request.createProperty(namespace,"has");
        Resource empty = request.createResource();
        request.add(subjectRequest,has,empty);

        Model database = ModelFactory.createDefaultModel();
        Resource base = database.createResource("MESSAGEBASE");
        Property hasDB = database.createProperty(namespace,"has");
        RDFNode[] items = {database.createLiteral("test")};
        RDFList result = database.createList(items);
        database.add(base,has,result);

        Model answer = ModelFactory.createDefaultModel();
        Resource subjectAnswer = answer.createResource("MESSAGEBASE");

        StmtIterator iterator = subjectRequest.listProperties();
        while(iterator.hasNext()){
            Statement requestStatement = iterator.nextStatement();
            if(database.contains(subjectAnswer,requestStatement.getPredicate())){
                RDFNode objectAnswer = database.getProperty(subjectAnswer,requestStatement.getPredicate()).getObject();
                RDFList list =  objectAnswer.as(RDFList.class).copy();
                System.out.println(list.getHead());
                answer.add(subjectAnswer,requestStatement.getPredicate(),list);
            }
        }
        answer.write(System.out,"TURTLE");
    }

    private static class OutgoingSelector extends SimpleSelector{
        private Resource subject;

        public OutgoingSelector(Resource subject){
            this.subject = subject;
        }

        @Override
        public boolean selects(Statement s) {
            return s.getSubject().equals(subject);
        }
    }
}
