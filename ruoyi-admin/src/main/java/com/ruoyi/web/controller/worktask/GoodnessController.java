package com.ruoyi.web.controller.worktask;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.exam.domain.LibraryDetail;
import com.ruoyi.framework.util.ShiroUtils;
import com.ruoyi.system.domain.SysUser;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.worktask.domain.Goodness;
import com.ruoyi.worktask.domain.SelfTask;
import com.ruoyi.worktask.domain.SelfTaskProcess;
import com.ruoyi.worktask.domain.WorkTaskFile;
import com.ruoyi.worktask.service.ISelfTaskProcessService;
import com.ruoyi.worktask.service.ISelfTaskService;
import com.ruoyi.worktask.service.IWorkTaskFileService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * 任务 信息操作处理
 * 
 * @author ruoyi
 * @date 2019-05-08
 */
@Controller
@RequestMapping("/worktask/goodness")
public class GoodnessController extends BaseController
{
    private String prefix = "worktask/goodness";
	@Autowired
	private IWorkTaskFileService workTaskFileService;
	@Autowired
	private ISelfTaskService selfTaskService;
	@Autowired
	private ISelfTaskProcessService selfTaskProcessService;
	@Autowired
	private ISysUserService userService;
	String seTaskType="3";
	@RequiresPermissions("worktask:goodness:view")
	@GetMapping()
	public String selfTask()
	{
		return prefix + "/selfTask";
	}

	/**
	 * 我创建的
	 */
	@RequiresPermissions("worktask:goodness:list")
	@PostMapping("/list")
	@ResponseBody
	public TableDataInfo list(Goodness selfTask)
	{
		startPage();
		selfTask.setSelvalTaskType(seTaskType);
		selfTask.setCreateBy(ShiroUtils.getUserId()+"");
		List<Goodness> list = selfTaskService.goodnessTaskList(selfTask);
		return getDataTable(list);
	}
	/**
	 * 共享任务
	 */
	@RequiresPermissions("worktask:goodness:list")
	@PostMapping("shareList")
	@ResponseBody
	public TableDataInfo shareList(Goodness selfTask)
	{
		startPage();
		selfTask.setSelvalTaskType(seTaskType);
		List<Goodness> list = selfTaskService.goodnessTaskList(selfTask);
		return getDataTable(list);
	}
	/**
	 * 我的任务
	 */
	@RequiresPermissions("worktask:goodness:list")
	@PostMapping("/myTask")
	@ResponseBody
	public TableDataInfo myTask(Goodness selfTask)
	{
		startPage();
		selfTask.setSelvalTaskType(seTaskType);
		selfTask.setExecutorUser(ShiroUtils.getUserId()+"");
		List<Goodness> list = selfTaskService.goodnessTaskList(selfTask);
		return getDataTable(list);
	}
	/**
	 * 我验收的任务
	 */
	@RequiresPermissions("worktask:goodness:list")
	@PostMapping("/myCheckTask")
	@ResponseBody
	public TableDataInfo myCheckTask(Goodness selfTask)
	{
		startPage();
		selfTask.setSelvalTaskType(seTaskType);
		selfTask.setAcceptorUser(ShiroUtils.getUserId()+"");
		List<Goodness> list = selfTaskService.goodnessTaskList(selfTask);
		return getDataTable(list);
	}

	/**
	 * 导出任务列表
	 */
	@RequiresPermissions("worktask:goodness:export")
	@PostMapping("/export")
	@ResponseBody
	public AjaxResult export(Goodness selfTask)
	{
		List<Goodness> list = selfTaskService.goodnessTaskList(selfTask);
		ExcelUtil<Goodness> util = new ExcelUtil<Goodness>(Goodness.class);
		return util.exportExcel(list, "selfTask");
	}

	/**
	 * 新增任务
	 */
	@GetMapping("/add")
	public String add(ModelMap modelMap)
	{
		List<SysUser> sysUsers = userService.selectUserList(new SysUser());
		modelMap.addAttribute("sysUsers",sysUsers);
		return prefix + "/add";
	}
	@GetMapping("/addChild/{pid}")
	public String addChild(ModelMap modelMap,@PathVariable("pid")  String pid)
	{
		List<SysUser> sysUsers = userService.selectUserList(new SysUser());
		modelMap.addAttribute("sysUsers",sysUsers);
		modelMap.addAttribute("pid",pid);
		return prefix + "/add";
	}

	/**
	 * 新增保存任务
	 */
	@RequiresPermissions("worktask:goodness:add")
	@Log(title = "任务", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	@ResponseBody
	public AjaxResult addSave(SelfTask selfTask)
	{
		selfTask.setSelvalTaskType(seTaskType);
		selfTask.setTaskStatus("0");
		selfTask.setTaskLevel("0");
		selfTask.setCreateBy(ShiroUtils.getUserId()+"");
		selfTask.setId(UUID.randomUUID().toString().replaceAll("-",""));
		return toAjax(selfTaskService.insertSelfTask(selfTask));
	}

	/**
	 * 修改任务
	 */
	@GetMapping("/edit/{id}")
	public String edit(@PathVariable("id") String id, ModelMap mmap)
	{
		List<SysUser> sysUsers = userService.selectUserList(new SysUser());
		mmap.addAttribute("sysUsers",sysUsers);
		Goodness selfTask = selfTaskService.selectGoodnessTaskById(id);
		SelfTaskProcess selfTaskProcess=new SelfTaskProcess();
		selfTaskProcess.setId(id);
		List<SelfTaskProcess> processList = selfTaskProcessService.selectSelfTaskProcessList(selfTaskProcess);
		mmap.put("processList", processList);
		mmap.put("selfTask", selfTask);
		return prefix + "/edit";
	}
	@GetMapping("/query/{id}/{taskType}")
	public String query(@PathVariable("id") String id,@PathVariable("taskType") String taskType, ModelMap mmap)
	{
		List<SysUser> sysUsers = userService.selectUserList(new SysUser());
		mmap.addAttribute("sysUsers",sysUsers);
		Goodness selfTask = selfTaskService.selectGoodnessTaskById(id);
		SelfTaskProcess selfTaskProcess=new SelfTaskProcess();
		selfTaskProcess.setId(id);
		List<SelfTaskProcess> processList = selfTaskProcessService.selectSelfTaskProcessList(selfTaskProcess);
		Goodness childSelfTask=new Goodness();
		childSelfTask.setPid(id);
		List<Goodness> childSelfTasks = selfTaskService.goodnessTaskList(childSelfTask);

		WorkTaskFile activityFile=new WorkTaskFile();
		activityFile.setWorkTaskId(id);
		List<WorkTaskFile> workTaskFiles = workTaskFileService.selectWorkTaskFileList(activityFile);
		mmap.put("workTaskFiles", workTaskFiles);
		mmap.put("taskType", taskType);
		mmap.put("processList", processList);
		mmap.put("selfTask", selfTask);
		mmap.put("childSelfTasks", childSelfTasks);
		return prefix + "/query";
	}
	/**
	 * 修改保存任务
	 */
	@RequiresPermissions("worktask:goodness:edit")
	@Log(title = "任务", businessType = BusinessType.UPDATE)
	@PostMapping("/edit")
	@ResponseBody
	public AjaxResult editSave(SelfTask selfTask,String check)
	{
		if(StringUtils.isNotEmpty(check)){
			if(check.equals("1")){//不通过
				selfTask.setTaskStatus("0");
				selfTask.setTaskProgress("0");
			}else if(check.equals("2")){//通过
				selfTask.setTaskStatus("2");
			}
		}
		return toAjax(selfTaskService.updateSelfTask(selfTask));
	}

	/**
	 * 删除任务
	 */
	@RequiresPermissions("worktask:goodness:remove")
	@Log(title = "任务", businessType = BusinessType.DELETE)
	@PostMapping( "/remove")
	@ResponseBody
	public AjaxResult remove(String ids)
	{
		return toAjax(selfTaskService.deleteSelfTaskByIds(ids));
	}
	@Log(title = "固优势补短板导入", businessType = BusinessType.IMPORT)
	@RequiresPermissions("worktask:goodness:import")
	@PostMapping("/importData")
	@ResponseBody
	public AjaxResult importData(MultipartFile file) throws Exception
	{
		ExcelUtil<Goodness> util = new ExcelUtil<Goodness>(Goodness.class);
		List<Goodness> goodnessList = util.importExcel(file.getInputStream());
		String operName = ShiroUtils.getSysUser().getUserId()+"";
		String message = selfTaskService.importGoodness(goodnessList, operName);
		return AjaxResult.success(message);
	}

	@RequiresPermissions("worktask:goodness:view")
	@GetMapping("/importTemplate")
	@ResponseBody
	public AjaxResult importTemplate()
	{
		ExcelUtil<Goodness> util = new ExcelUtil<Goodness>(Goodness.class);
		return util.importTemplateExcel("固优势补短板");
	}
}

	

