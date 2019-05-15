package pt.ist.cmu.helpers;

public class StringGenerator {
    public static String generateName(int n) {

        // chose a Character random from this String
        String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {
            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index = (int)(alphaNumericString.length() * Math.random());

            // add Character one by one in end of sb
            sb.append(alphaNumericString.charAt(index));
        }

        return sb.toString();
    }
}
