import com.nlg.common.NLGException;

public class Main {

	public static void main(String[] args) throws NLGException {
		String xml = "";
		xml = "<question_type>WHAT</question_type>" +
		      "<error>" +
		      "<error_id>ORA-000001</error_id>" +
		      "<is_fixable_by_the_system>false</is_fixable_by_the_system>" +
		      "<caused_due_to>Table owner name not specified when logged-in as a non-creator of the table, " +
		      "ORA-00942 on table import (imp or impdp), ORA-00942 on materialized view refresh" +
		      "</caused_due_to>" +
		      "<severity_level>minor</severity_level>" +
		      "<ran>quick</ran>" +
		      "<error_description>the table or view does not exists</error_description>" +
		      "<ran>quiet</ran>" +
		      "<ran>slow</ran>" +
		      "<need_Oracle_support_to_fix>false</need_Oracle_support_to_fix>" +
		      "<is_clear>false</is_clear>" +
		      "</error>";
		/* xml= "<question_type>WHAT</question_type><file><file_name>listner.ora</file_name><located_in>/usr/local/ora" +
		      "</located_in>" +
		             "<description>checks whether the oracle db is up and running</description></file>" ;*/

/*		 xml =
		"<question_type>HOW</question_type><steps><commandDesc>Start listener, Stop " +
		"listener</commandDesc><commands>lsnrctl start, lsnrctl stop</commands></steps>" ;*/

		/*xml =
				"<question_type>WHY</question_type><error><error_id>ora-1234</error_id><caused_due_to>Table or view " +
				"does not exists</caused_due_to>" +
				"</error>" ;*/
		/*xml = "<question_type>WHERE</question_type><file><file_name>ora-1234</file_name><located_in>/usr/document/" +
		      "</located_in></file>";*/
		SentencePlanner sentencePlanner = new SentencePlanner();
		try {
			System.out.println(sentencePlanner.createAnswer(xml));
		} catch (NLGException e) {
			e.printStackTrace();
		}
		/*Lexicon lexicon = Lexicon.getDefaultLexicon();
        NLGFactory nlgFactory = new NLGFactory(lexicon);
		SPhraseSpec sPhraseSpec = new SPhraseSpec(nlgFactory);
		sPhraseSpec.setVerb("cook");
		//sPhraseSpec.setNegated(true);
        //NLGElement s1 = nlgFactory.createClause("error", "cook");
        //s1.setFeature(Feature.FORM, Tense.PRESENT);
       // s1.setFeature(Feature.POSSESSIVE, true);
        Realiser realiser = new Realiser(lexicon);
        realiser.realise(sPhraseSpec);

        *//*Random random = new Random();
        int randomCount = random.nextInt(3);*//*

       System.out.println(realiser.realiseSentence(sPhraseSpec));*/
		/*WordFormIdentifier stemmer = new WordFormIdentifier();
		System.out.println(stemmer.getPronoun("table"));*/
/*        WordFormIdentifier stemmer = null;
        try {
            stemmer = new WordFormIdentifier();
            System.out.println(stemmer.getPronoun("place"));
        } catch (NLGException e) {
            e.printStackTrace();
        }*/

	}
}
