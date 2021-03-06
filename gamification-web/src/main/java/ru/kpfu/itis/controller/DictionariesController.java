package ru.kpfu.itis.controller;

import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.kpfu.itis.dto.AccountDto;
import ru.kpfu.itis.dto.CourseOrGroupDto;
import ru.kpfu.itis.dto.SubjectDto;
import ru.kpfu.itis.mapper.AccountDtoMapper;
import ru.kpfu.itis.mapper.SubjectDtoMapper;
import ru.kpfu.itis.model.*;
import ru.kpfu.itis.processing.SimpleService;
import ru.kpfu.itis.security.SecurityService;
import ru.kpfu.itis.service.AccountService;
import ru.kpfu.itis.util.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Roman on 21.08.2015.
 */
@Controller
@RequestMapping(Constant.API_URI_PREFIX + "/dictionaries")
public class DictionariesController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private SimpleService simpleService;

    @Autowired
    private SecurityService securityService;

    @RequestMapping(value = "/teachers", method = RequestMethod.GET)
    @ResponseBody
    public List<AccountDto> getTeachers() {
        List<Account> teachers = accountService.getTeachers();
        return teachers.stream().map(new AccountDtoMapper()).collect(Collectors.toList());
    }

    @RequestMapping(value = "/students", method = RequestMethod.GET)
    @ApiImplicitParams(value = {@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "string", paramType = "query")})
    @ResponseBody
    public List<AccountDto> getStudents() {
        List<Account> students = accountService.getStudents(securityService.getCurrentUserId());
        return students.stream().map(new AccountDtoMapper()).collect(Collectors.toList());
    }

    @RequestMapping(value = "/students/{groups}", method = RequestMethod.GET)
    @ResponseBody
    public List<AccountDto> getStudentsByGroups(@PathVariable String[] groups) {
        List<Account> students = accountService.getStudentsByGroups(groups);
        return students.stream().map(new AccountDtoMapper()).collect(Collectors.toList());
    }

    @RequestMapping(value = "/disciplines", method = RequestMethod.GET)
    @ApiImplicitParams(value = {@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "string", paramType = "query")})
    @ResponseBody
    public List<SubjectDto> getDisciplines() {
        List<Subject> subjects = accountService.getSubjects(securityService.getCurrentUser());
        return subjects.stream().map(new SubjectDtoMapper()).collect(Collectors.toList());
    }

    @RequestMapping(value = "/getCoursesAndGroups", method = RequestMethod.GET)
    @ApiImplicitParams(value = {@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "string", paramType = "query")})
    @ResponseBody
    public List<CourseOrGroupDto> getCoursesAndGroups() {
        List<AcademicGroup> academicGroups = accountService.getAcademicGroups(securityService.getCurrentUser());
        List<StudyCourse> studyCourses = accountService.getStudyCourses(securityService.getCurrentUser());
        return prepareResultList(academicGroups, studyCourses);
    }


    public List<CourseOrGroupDto> prepareResultList(List<AcademicGroup> academicGroups, List<StudyCourse> studyCourses) {
        List<CourseOrGroupDto> resultList = new ArrayList<>();
        for (AcademicGroup academicGroup : academicGroups) {
            CourseOrGroupDto courseOrGroupDto = new CourseOrGroupDto();
            courseOrGroupDto.setIsGroup(true);
            courseOrGroupDto.setId(academicGroup.getId());
            courseOrGroupDto.setNumber(academicGroup.getName());
            resultList.add(courseOrGroupDto);
        }
        for (StudyCourse studyCourse : studyCourses) {
            CourseOrGroupDto courseOrGroupDto = new CourseOrGroupDto();
            courseOrGroupDto.setNumber(studyCourse.getNumber() + " курс");
            courseOrGroupDto.setIsGroup(false);
            courseOrGroupDto.setId(studyCourse.getId());
            resultList.add(courseOrGroupDto);
        }
        return resultList;
    }

}

