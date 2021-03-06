package com.ruoyi.web.controller.activiti;


import com.ruoyi.activiti.domain.TaskVO;
import com.ruoyi.activiti.service.ActTaskService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.framework.util.ShiroUtils;
import com.ruoyi.system.domain.EchartVo;
import com.ruoyi.worktask.domain.WorkTask;
import com.ruoyi.worktask.domain.WorkTaskActivity;
import com.ruoyi.worktask.service.IWorkTaskActivityService;
import com.ruoyi.worktask.service.IWorkTaskService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.TaskQuery;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/***
 * 代办任务
 */
@RequestMapping("activiti/task")
@Controller
public class TaskController extends BaseController {
    @Autowired
    private TaskService taskService;
    @Autowired
    ActTaskService actTaskService;

    private String prefix = "activiti/task";
    /**
     * 进入代办任务页
     * @return
     */
    @RequiresPermissions("activiti:task:view")
    @GetMapping
    public   String task() {
        return prefix + "/tasks";
    }

    /**
     * 查询待办的专项工作
     * @return
     */
    @PostMapping("/getTaskToDoCount")
    @ResponseBody
    public AjaxResult getTaskToDoCount(TaskVO taskVO)
    {
        List<TaskVO> list=new ArrayList<TaskVO>();
        taskVO.setAssignee(ShiroUtils.getLoginName());
        List<TaskVO> taskVOS = actTaskService.taskCandidateOrAssigned(taskVO);
        Iterator<TaskVO> taskVOIterator = taskVOS.iterator();
        while (taskVOIterator.hasNext()){
            TaskVO taskV = taskVOIterator.next();
            String processId = taskV.getProcessId();
            WorkTaskActivity workTaskActivity = workTaskActivityService.selectWorkTaskActivityByProId(processId);
            if(workTaskActivity!=null){
                list.add(taskV);
            }
        }
//        TaskQuery taskQuery = taskService.createTaskQuery().taskCandidateOrAssigned((ShiroUtils.getLoginName()));
//        long count = taskQuery.count();
        AjaxResult ajaxResult=new AjaxResult();
        ajaxResult.put("data",list.size());
        return ajaxResult;
    }

    /**
     * 查看我的任务  admin 查看所有任务
     * @return
     */
    @Log(title = "查询任务", businessType = BusinessType.OTHER)
    @RequiresPermissions("activiti:task:view")
    @RequestMapping("/list")
    @ResponseBody
    TableDataInfo list(TaskVO taskVO) {
        List<TaskVO> list=new ArrayList<TaskVO>();
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        taskVO.paging()[0]=pageNum;
        taskVO.paging()[1]=pageSize;
        taskVO.setProcessInstanceBusinessKey("1");
//        taskVO.setCandidateUser(ShiroUtils.getLoginName());
        taskVO.setAssignee(ShiroUtils.getLoginName());
        List<TaskVO> taskVOS = actTaskService.taskCandidateOrAssigned(taskVO);
        Iterator<TaskVO> taskVOIterator = taskVOS.iterator();
        while (taskVOIterator.hasNext()){
            TaskVO taskV = taskVOIterator.next();
            String processId = taskV.getProcessId();
            WorkTaskActivity workTaskActivity = workTaskActivityService.selectWorkTaskActivityByProId(processId);
            if(workTaskActivity!=null){
                String workTaskId = workTaskActivity.getWorkTaskId();
                WorkTask workTask = workTaskService.selectWorkTaskById(workTaskId);
                taskV.setName(workTask.getWorkName()+"("+workTaskActivity.getTargetMonth()+"月)");
                taskV.setProcessInstanceBusinessKey(workTaskActivity.getId());
                list.add(taskV);
            }
        }
        TableDataInfo dataTable = getDataTable(list);
        dataTable.setTotal(list.size());
        return dataTable;
    }

    @RequiresPermissions("activiti:task:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }


    @RequiresPermissions("activiti:task:edit")
    @PostMapping("/edit/{taskId}")
    @ResponseBody
    public String edit(@PathVariable("taskId") String taskId, ModelMap map) {
        TaskVO taskVO = actTaskService.selectOneTask(taskId);
        map.put("task", taskVO);
        return prefix + "/edit";
    }


    @Log(title = "更新任务", businessType = BusinessType.OTHER)
    @RequiresPermissions("activiti:task:edit")
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(TaskVO task, ModelMap map) {
        actTaskService.completeTask(task.getId(), map);
        return toAjax(1);
    }

    @Log(title = "代办任务", businessType = BusinessType.OTHER)
    @RequiresPermissions("activiti:task:view")
    @PostMapping("/todo")
    @ResponseBody
    public TableDataInfo todo(TaskVO task,ModelMap map) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        task.paging()[0]=pageNum;
        task.paging()[1]=pageSize;
        task.setAssignee(ShiroUtils.getLoginName());
        List<TaskVO> taskVOs = actTaskService.selectTodoTask(task);
        Iterator<TaskVO> taskVOIterator = taskVOs.iterator();
        while (taskVOIterator.hasNext()){
            TaskVO taskV = taskVOIterator.next();
            String processId = taskV.getProcessId();
            WorkTaskActivity workTaskActivity = workTaskActivityService.selectWorkTaskActivityByProId(processId);
            if(workTaskActivity!=null){
                String workTaskId = workTaskActivity.getWorkTaskId();
                WorkTask workTask = workTaskService.selectWorkTaskById(workTaskId);
                taskV.setName(workTask.getWorkName()+"("+workTaskActivity.getTargetMonth()+"月)");
            }
        }
        TableDataInfo dataTable = getDataTable(taskVOs);
        dataTable.setTotal(task.getCount());
        return dataTable;
    }
    @Log(title = "受邀任务", businessType = BusinessType.OTHER)
    @RequiresPermissions("activiti:task:view")
    @PostMapping("/involved")
    @ResponseBody
    public TableDataInfo selectInvolvedTask(TaskVO task,ModelMap map) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        task.paging()[0]=pageNum;
        task.paging()[1]=pageSize;
        task.setInvolvedUser(ShiroUtils.getLoginName());
        List<TaskVO> taskVOs = actTaskService.selectInvolvedTask(task);
        TableDataInfo dataTable = getDataTable(taskVOs);
        dataTable.setTotal(task.getCount());
        return dataTable;
    }
    @Log(title = "归档任务", businessType = BusinessType.OTHER)
    @RequiresPermissions("activiti:task:view")
    @PostMapping("/archived")
    @ResponseBody
    public TableDataInfo selectArchivedTask(TaskVO task,ModelMap map) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        task.paging()[0]=pageNum;
        task.paging()[1]=pageSize;
        task.setOwner(ShiroUtils.getLoginName());
        List<TaskVO> taskVOs = actTaskService.selectArchivedTask(task);
        TableDataInfo dataTable = getDataTable(taskVOs);
        dataTable.setTotal(task.getCount());
        return dataTable;
    }
    @Autowired
    private IWorkTaskActivityService workTaskActivityService;
    @Autowired
    private IWorkTaskService workTaskService;
    @Log(title = "查询完成的任务", businessType = BusinessType.OTHER)
    @RequiresPermissions("activiti:task:view")
    @ResponseBody
    @PostMapping("/finishedTask")
    public TableDataInfo finishedTask(TaskVO task, ModelMap map) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        task.paging()[0]=pageNum;
        task.paging()[1]=pageSize;
//        task.setAssignee(ShiroUtils.getLoginName());
        task.setOwner(ShiroUtils.getLoginName());
        task.setProcessInstanceBusinessKey("1");
        List<TaskVO> taskVOs = actTaskService.selectFinishedTask(task);
        Iterator<TaskVO> taskVOIterator = taskVOs.iterator();
        while (taskVOIterator.hasNext()){
            TaskVO taskVO = taskVOIterator.next();
            String processId = taskVO.getProcessId();
            WorkTaskActivity workTaskActivity = workTaskActivityService.selectWorkTaskActivityByProId(processId);
            if(workTaskActivity!=null){
                String workTaskId = workTaskActivity.getWorkTaskId();
                WorkTask workTask = workTaskService.selectWorkTaskById(workTaskId);
                taskVO.setName(workTask.getWorkName()+"("+workTaskActivity.getTargetMonth()+"月)");
                taskVO.setProcessInstanceBusinessKey(workTaskActivity.getId());
            }
        }
        TableDataInfo dataTable = getDataTable(taskVOs);
        dataTable.setTotal(task.getCount());
        return dataTable;
    }


    @GetMapping("/form/{procDefId}")
    public void startForm(@PathVariable("procDefId") String procDefId, HttpServletResponse response) throws IOException {
        String formKey = actTaskService.getFormKey(procDefId, null);
        response.sendRedirect(formKey);
    }

    @GetMapping("/form/{procDefId}/{taskId}")
    public void form(@PathVariable("procDefId") String procDefId, @PathVariable("taskId") String taskId, HttpServletResponse response) throws IOException {
        // 获取流程XML上的表单KEY
        String formKey = actTaskService.getFormKey(procDefId, taskId);
        System.out.println("完成任务");
        Map<String, Object> vars=new HashMap<String, Object>();
        vars.put("chengyuan_users","admin,15693120282,18919818967");
        actTaskService.completeTask(taskId,vars);
        response.sendRedirect(formKey + "/" + taskId);
    }



    /**
     * 读取带跟踪的图片
     */
    @RequestMapping(value = "/trace/photo/{procDefId}/{execId}")
    public void traceTaskPhoto(@PathVariable("procDefId") String procDefId, @PathVariable("execId") String execId, HttpServletResponse response) throws Exception {
        InputStream imageStream = actTaskService.traceTaskPhoto(procDefId, execId);
        if(imageStream==null){
            return;
        }
        // 输出资源内容到相应对象
        byte[] b = new byte[1024];
        int len;
        while ((len = imageStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }


}
