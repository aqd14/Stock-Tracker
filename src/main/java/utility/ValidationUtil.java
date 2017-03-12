package main.java.utility;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jfoenix.controls.JFXDatePicker;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import main.java.common.CommonDefine;
import main.java.dao.UserManager;
import main.java.model.User;

public class ValidationUtil {
	
	static UserManager<User> userManager = new UserManager<User>();
	
	
	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
	
	public static final Pattern VALID_CHARACTERS_REGEX = Pattern.compile("^[a-zA-Z]+$");
	
	public static final Pattern PHONE_NUMBER_REGEX = Pattern.compile("^\\(?([0-9]{3})\\)?[-.\\s]?([0-9]{3})[-.\\s]?([0-9]{4})$");
	
	public ValidationUtil() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Check if given text contains only numbers
	 * @param txt
	 * @return
	 */
	public static boolean containOnlyNumber(String txt) {
		return txt.matches("^[0-9]+$");
	}
	
	/**
	 * Validate user's first name.
	 * It shouldn't be empty. It shouldn't have special characters (!,@,#,$,%,&,*..)
	 */
	public static boolean validateFirstName(TextField firstNameTF, Text firstNameError) {
		// Validate first name's empty
		if (isTextFieldEmpty(firstNameTF)) {
			displayErrorMessage(firstNameError, CommonDefine.EMPTY_FIELD_ERR);
			return false;
		} 
		
		if (!isNameValid(firstNameTF.getText())) {
			displayErrorMessage(firstNameError, CommonDefine.INVALID_NAME_ERR);
			return false;
		}
		// Valid first name
		hideErrorMessage(firstNameError);
		return true;
	}
	
	/**
	 * Last name is validate through several phases.
	 * If it fails in one phase, the error message corresponding to
	 * that phase will be displayed. Function stops validating.
	 */
	public static boolean validateLastName(TextField lastNameTF, Text lastNameError) {
		// Validate first name
		if (isTextFieldEmpty(lastNameTF)) {
			displayErrorMessage(lastNameError, CommonDefine.EMPTY_FIELD_ERR);
			return false;
		} 
		
		if (!isNameValid(lastNameTF.getText())) {
			displayErrorMessage(lastNameError, CommonDefine.INVALID_NAME_ERR);
			return false;
		}
		
		// Passed all phases, set error text invisible
		hideErrorMessage(lastNameError);
		return true;
	}
	
	public static boolean validateUsername(TextField usernameTF, Text usernameError) {
		// Validate first name
		if (isTextFieldEmpty(usernameTF)) {
			displayErrorMessage(usernameError, CommonDefine.EMPTY_FIELD_ERR);
			return false;
		}
		
		boolean userAlreadyExisted = false;
		if (userAlreadyExisted) {
			// Search through all database to find if user already existed
			// Print warning message
			return false;
		}
		
		// Check if username contains invalid characters
		boolean isContainedInvalidChar = false; 
		if (isContainedInvalidChar) {
			// Print warning message
			return false;
		}
		
		final int minLength = 6;
		final int maxLength = 15;
		if (usernameTF.getText().length() < minLength || usernameTF.getText().length() > maxLength) { // Length should be from 6 - 30 (Eg)
			// Print warning message
			displayErrorMessage(usernameError, CommonDefine.INVALID_USERNAME_LENGTH_ERR);
			return false;
		}
		hideErrorMessage(usernameError);
		return true;
	}
	
	/**
	 * Validate current password when user changes password in [Settings]
	 * 
	 * @param passwordPF
	 * @param passwordError
	 * @return
	 */
	public static boolean validateCurrentPassword(String curPw, PasswordField passwordPF, Text passwordError) {
		if (!curPw.equals(passwordPF.getText())) {
			passwordError.setText(CommonDefine.CURRENT_PASSWORD_INCORRECT);
			passwordError.setVisible(true);
			return false;
		}
		hideErrorMessage(passwordError);
		return true;
	}
	
	/**
	 * Validate password when user registers new account, resets or changes password
	 * @param passwordPF
	 * @param passwordError
	 * @return
	 */
	public static boolean validateOriginalPassword(PasswordField passwordPF, Text passwordError) {
		// Validate empty
		if (isTextFieldEmpty(passwordPF)) {
			passwordError.setText(CommonDefine.EMPTY_FIELD_ERR);
			passwordError.setVisible(true);
			return false;
		}
		
		final int minLength = 8;
		if(passwordPF.getText().length() < minLength) {
			displayErrorMessage(passwordError, CommonDefine.PASSWORD_TOO_SHORT_ERR);
			return false;
		}
		// Passed all validation
		hideErrorMessage(passwordError);
		return true;
	}
	
	/**
	 * Validate if confirm password matches entered password
	 * @param passwordPF
	 * @param confirmPasswordPF
	 * @param confirmPasswordError
	 * @return
	 */
	public static boolean validateConfirmedPassword(PasswordField passwordPF, PasswordField confirmPasswordPF, Text confirmPasswordError) {
		if (isTextFieldEmpty(confirmPasswordPF)) {
			displayErrorMessage(confirmPasswordError, CommonDefine.EMPTY_FIELD_ERR);
			return false;
		}
		
		if (!passwordPF.getText().equals(confirmPasswordPF.getText())) {
			System.err.println("Passwords not matching!");
			displayErrorMessage(confirmPasswordError, CommonDefine.PASSWORD_NOT_MATCHED_ERR);
			// Reset password fields to empty
			passwordPF.setText("");
			confirmPasswordPF.setText("");
			return false;
		}
		// Passed all validation
		hideErrorMessage(confirmPasswordError);
		return true;
	}
	
	/**
	 * Validate email address:
	 * <p><ul>
	 * 	<li> Is email empty?
	 *  <li> Is email valid (contains only alphabets, underscore, and period)
	 *  <li> Is email already existing on database?
	 *  <ul><p>
	 */
	public static boolean validateEmail(TextField emailTF, Text emailError) {
		if (isTextFieldEmpty(emailTF)) {
			displayErrorMessage(emailError, CommonDefine.EMPTY_FIELD_ERR);
			return false;
		}
		
		if (!isValidEmail(emailTF.getText())) {
			displayErrorMessage(emailError, CommonDefine.INVALID_EMAIL_ERR);
			return false;
		}
		
		User user = userManager.findByEmail(emailTF.getText());
		if (null != user) {
			displayErrorMessage(emailError, CommonDefine.EMAIL_TAKEN_ERR);
			return false;
		}
		// Passed all validations
		hideErrorMessage(emailError);
		return true;
	}
	
	/**
	 * Validate email address:
	 * <p><ul>
	 * 	<li> Is email empty?
	 *  <li> Is email valid (contains only alphabets, underscore, and period)
	 *  <li> Is email already existing on database?
	 *  <ul><p>
	 */
	public static boolean validatePhoneNumber(TextField phoneNumberTF) {
		if (isTextFieldEmpty(phoneNumberTF)) {
//			displayErrorMessage(phoneNumberError, CommonDefine.EMPTY_FIELD_ERR);
			return false;
		}
		
		if (!isPhoneNumberValid(phoneNumberTF.getText())) {
//			displayErrorMessage(phoneNumberError, CommonDefine.INVALID_EMAIL_ERR);
			return false;
		}
		
		// Passed all validations
//		hideErrorMessage(phoneNumberError);
		return true;
	}
	
	public static boolean validateDoB(JFXDatePicker dateOfBirthDP, Text dobError) {
		LocalDate dob = dateOfBirthDP.getValue();
		if (null == dob) {
			displayErrorMessage(dobError, CommonDefine.EMPTY_FIELD_ERR);
			return false;
		}
		LocalDate maxDate = LocalDate.of(2000, 1, 1);
		LocalDate minDate = LocalDate.of(1950, 1, 1);
		if (dob.isAfter(maxDate)) {
			displayErrorMessage(dobError, "You are too young to play stock!");
			return false;
		}
		
		if (dob.isBefore(minDate)) {
			displayErrorMessage(dobError, "You are too old to play stock!");
			return false;
		}
		
		hideErrorMessage(dobError);
		return true;
	}
	
	public static void displayErrorMessage(Text instance, String errMessage) {
		instance.setText(errMessage);
		instance.setVisible(true);
	}
	
	public static void hideErrorMessage(Text instance) {
		instance.setVisible(false);   
	}
	
	/**
	 * User's name should only contains alphabetical characters
	 * @param instance
	 * @return
	 */
	public static boolean isPhoneNumberValid(String instance) {
		Matcher matcher = PHONE_NUMBER_REGEX.matcher(instance);
		return matcher.find();
	}
	
	/**
	 * Verify if the given text field is empty or not
	 * @param tf Given TextField instance
	 * @return True if not empty, otherwise returns False
	 */
	public static boolean isTextFieldEmpty(TextField tf) {
		if (tf == null) {
			return true;
		}
		return tf.getText().equals("");
	}
	
	/**
	 * Validate email address by using regular expression
	 * @param email
	 * @return True if email is valid, otherwise return False
	 */
	public static boolean isValidEmail(String email) {
	        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
	        return matcher.find();
	}
	
	/**
	 * User's name should only contains alphabetical characters
	 * @param instance
	 * @return
	 */
	public static boolean isNameValid(String instance) {
		Matcher matcher = VALID_CHARACTERS_REGEX.matcher(instance);
		return matcher.find();
	}
}
