-- User Profile Extension Tables
-- Run these SQL commands in your MySQL database to create the new tables

-- Table for User Basic Information
CREATE TABLE IF NOT EXISTS user_basic_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    mobile_number VARCHAR(15),
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    date_of_birth DATE,
    experience_years INT DEFAULT 0,
    experience_months INT DEFAULT 0,
    annual_salary_lakhs DECIMAL(10,2) DEFAULT 0,
    annual_salary_thousands DECIMAL(10,2) DEFAULT 0,
    skills TEXT,
    job_function VARCHAR(100),
    current_location VARCHAR(100),
    preferred_location VARCHAR(100),
    resume_file_path VARCHAR(500),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_basic_info_user_id (user_id)
);

-- Table for User Education Information
CREATE TABLE IF NOT EXISTS user_education (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    qualification VARCHAR(100) NOT NULL,
    course VARCHAR(100) NOT NULL,
    specialization VARCHAR(100),
    percentage DOUBLE,
    college_school VARCHAR(200),
    university_board VARCHAR(200),
    course_type ENUM('FULL_TIME', 'PART_TIME', 'CORRESPONDENCE'),
    passing_year INT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_education_user_id (user_id),
    INDEX idx_user_education_passing_year (passing_year)
);

-- Table for User Employment Information
CREATE TABLE IF NOT EXISTS user_employment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    organization_type ENUM('CURRENT', 'PREVIOUS') NOT NULL,
    designation VARCHAR(100) NOT NULL,
    working_since DATE NOT NULL,
    location VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_employment_user_id (user_id),
    INDEX idx_user_employment_working_since (working_since)
);

-- Add some sample data (optional)
-- You can uncomment these lines to add sample data for testing

/*
-- Sample Basic Info (replace user_id with actual user ID)
INSERT INTO user_basic_info (user_id, mobile_number, gender, date_of_birth, experience_years, experience_months, 
                            annual_salary_lakhs, annual_salary_thousands, skills, job_function, 
                            current_location, preferred_location) 
VALUES (1, '9876543210', 'MALE', '1990-01-15', 5, 6, 12, 50, 
        '["Java", "Spring Boot", "Angular", "MySQL"]', 'Software Engineer', 
        'Bangalore', 'Bangalore');

-- Sample Education (replace user_id with actual user ID)
INSERT INTO user_education (user_id, qualification, course, specialization, percentage, 
                           college_school, university_board, course_type, passing_year)
VALUES (1, 'Bachelor of Technology', 'Computer Science', 'Software Engineering', 85.5, 
        'ABC Engineering College', 'XYZ University', 'FULL_TIME', 2018);

-- Sample Employment (replace user_id with actual user ID)
INSERT INTO user_employment (user_id, company_name, organization_type, designation, working_since, location)
VALUES (1, 'Tech Solutions Pvt Ltd', 'CURRENT', 'Senior Software Engineer', '2020-06-01', 'Bangalore');
*/ 