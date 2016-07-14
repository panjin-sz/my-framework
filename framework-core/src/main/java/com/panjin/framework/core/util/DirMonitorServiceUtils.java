/**
 * Copyright (c) 2011-2016 All Rights Reserved.
 */
package com.panjin.framework.core.util;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.panjin.framework.basic.log.Log;
import com.panjin.framework.basic.util.Pair;

/**
 * 文件目录watch服务
 *
 * @author panjin
 * @version $Id: DirMonitorServiceUtils.java 2016年7月14日 下午4:56:13 $
 */
public final class DirMonitorServiceUtils {
private static final Logger logger = LoggerFactory.getLogger(DirMonitorServiceUtils.class);
    
    private DirMonitorServiceUtils(){}

    /**
     * 注册目录监控
     * @param dir 目录
     * @param monitor 监控处理
     * @return
     */
    public static boolean watch(Path dir, IFileMonitor monitor){
        try {
            WatchDir.getInstance().register(dir, monitor);
            return true; 
        } catch (IOException e) {
            logger.error(Log.op("register_filemonitor_io_exception").msg(e.getMessage())
                      .kv("className", DirMonitorServiceUtils.class.getName())
                      .kv("method", "watch").toString(), e);
        }
        return false; 
    }

    /**
     * 单例延迟初始化ThreadPool
     * 
     */
    private static class ExecutorServiceHolder {
        private static final ExecutorService executor = Executors.newCachedThreadPool();
    }
    
    /**
     * 目录监控
     *
     */
    private final static class WatchDir {
        private static final Logger logger = LoggerFactory.getLogger(WatchDir.class);
        private final Map<WatchKey, Pair<Path, List<IFileMonitor>>> keys;
        private WatchService watcher;
        private boolean isClosed = true;

        private volatile static WatchDir watchDir = null;

        private WatchDir() throws IOException {
            this.watcher = FileSystems.getDefault().newWatchService();
            this.keys = new HashMap<>();
        }
        
        
        public static WatchDir getInstance(){
            //同步单例创建
            synchronized (WatchDir.class) {
                if(watchDir == null){
                    try {
                        watchDir = new WatchDir();
                    } catch (IOException e) {
                        logger.error(Log.op("watch_dir_io_exception").msg("create_watchdir_instance_error")
                                  .kv("className", DirMonitorServiceUtils.class.getName())
                                  .kv("method", "watch")
                                  .kv("exception", e.getMessage()).toString(), e);
                    }
                }
            }
            return watchDir; 
        }
        
        public void register(Path path, IFileMonitor monitor) throws IOException{
            if(path== null || monitor == null){
                throw new IllegalArgumentException("register required path and processor parameters not null!");
            }
            //同步注册
            synchronized (this) {
                if(watcher == null){
                    watcher = FileSystems.getDefault().newWatchService();
                }
                if (monitor.isRecursive()) {
                    innerRegisterAll(path, monitor);
                } else {
                    innerRegister(path, monitor);
                }
                
                if(isClosed){
                    ExecutorServiceHolder.executor.execute(new Runnable() {
                        public void run() {
                            processEvents();
                        }
                    });
                    isClosed = false; 
                }
            }
        }
        
        /**
         * 注册监控目录
         * 
         * @param dir
         * @throws IOException
         */
        private void innerRegister(Path dir, IFileMonitor monitor) throws IOException {
            // 相同的目录注册时，dir == dir2 false 但是 WatchKey是相同一个
            WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

            Pair<Path, List<IFileMonitor>> value = getValue(key);
            if (value == null) {
                // 同步keys
                synchronized (keys) {
                    value = keys.get(key);
                    if (value == null) {
                        synchronized (keys) {
                            value = Pair.of(dir, Collections.synchronizedList(new LinkedList<IFileMonitor>()));
                            keys.put(key, value);
                        }
                    }
                }
            }
            List<IFileMonitor> monitorList = value.second();
            if (!monitorList.contains(monitor)) {
                monitorList.add(monitor);
            }
        }
        
        private Pair<Path, List<IFileMonitor>> getValue(WatchKey key){
            Pair<Path, List<IFileMonitor>> value = null;  
            synchronized (keys) {
                value = keys.get(key);
            }
            return value; 
        }
        
        private void removeKey(WatchKey key){
            synchronized (keys) {
                keys.remove(key);
                key.cancel();
            }
        }

        private boolean isKeysEmpty(){
            boolean isEmpty = false; 
            synchronized (keys) {
                isEmpty = this.keys.isEmpty();
            }
            return isEmpty; 
        }
        /**
         * 递归注册监控目录
         * 
         * @param start
         * @throws IOException
         */
        private void innerRegisterAll(final Path start,final IFileMonitor monitor) throws IOException {
            Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    innerRegister(dir, monitor);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        
        /**
         * 事件监控处理
         */
        private void processEvents() {
            for (;;) {
                // 等待事件
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException x) {
                    return;
                }

                Pair<Path, List<IFileMonitor>> value = getValue(key);
                if(value == null){
                    logger.error(Log.op("dir_watch_no_processor").msg("watchkey_not_recognized")
                              .kv("className", DirMonitorServiceUtils.class.getName()).kv("method", "processEvents")
                              .toString());
                    continue; 
                }
                
                processEvent(key, value);

                // reset不成功，代表目录无法访问，删除对应的监控key
                boolean valid = key.reset();
                if (!valid) {
                    removeKey(key);
                    //如果所有的
                    if (isKeysEmpty()) {
                        close();
                        break;
                    }
                }
            }
            
        }

        private void processEvent(WatchKey key, Pair<Path, List<IFileMonitor>> value) {
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                // Context for directory entry event is the file name of
                // entry
                WatchEvent<Path> ev = cast(event);

                if (kind == OVERFLOW) {
                    continue;
                }
                Path dir = value.first(); 
                Path name = ev.context();
                Path child = dir.resolve(name);
                
                List<IFileMonitor> removeList = new ArrayList<IFileMonitor>(); 
                for (IFileMonitor monitor: value.second()) {
                    if(monitor != null){
                        if(monitor.match(name.toString(), child.toString(), ev.kind())){
                            monitor.process(name, child, ev.kind());
                        }
                        
                        // 如果创建文件目录，并且配置为递归监控，加入子目录
                        if (monitor.isRecursive() && (kind == ENTRY_CREATE)) {
                            try {
                                if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                                    innerRegisterAll(child, monitor);
                                }
                            } catch (IOException x) {
                                logger.error(Log.op("recursive_register_monitor_fail")
                                        .msg(x.getMessage())
                                        .kv("className", WatchDir.class)
                                        .kv("method", "processEvent").toString(), x);
                            }
                        }
                        if(monitor.isClosed()){
                            removeList.add(monitor); 
                        }
                    }
                }
                if(removeList.size() > 0){
                    value.second().removeAll(removeList);
                    if(value.second().isEmpty()){
                        removeKey(key);
                    }
                }
            }
        }

        public void close() {
            try{
                logger.info(Log.op("close_watcher")
                        .kv("className", WatchDir.class)
                        .kv("method", "processEvent").toString());
                watcher.close();
            }catch(IOException ex){
                logger.error(Log.op("close_watcher_exception")
                        .msg(ex.getMessage())
                        .kv("className", WatchDir.class)
                        .kv("method", "processEvent").toString(), ex);
            }finally{
                isClosed = true; 
                watcher = null; 
            }
        }

        @SuppressWarnings("unchecked")
        static <T> WatchEvent<T> cast(WatchEvent<?> event) {
            return (WatchEvent<T>) event;
        }
    }
    
    /**
     * 
     */
    public static interface IFileMonitor {
        /**
         * 是否递归监控子目录
         * @return
         */
        public boolean isRecursive();
        /**
         * 是否已经closed
         * @return
         */
        public boolean isClosed();
        /**
         * 是否匹配
         * @param fileName 文件名
         * @param fileAbsolutePath 绝对路径
         * @param kind 变更类型
         * @return
         */
        public boolean match(String fileName, String fileAbsolutePath, Kind<Path> kind);
        /**
         * 处理
         * @param file 文件路径  toString后是文件名称
         * @param resolvePath 解析后的路径   toString后是文件绝对路径    
         * @param eventKind 变更类型
         */
        public void process(Path file, Path resolvePath, Kind<Path> eventKind); 
    }

    /**
     * 默认实现
     *
     */
    public static abstract class  AbstractFileMonitor implements IFileMonitor{ 
        @Override
        public boolean isClosed() {
            return false;
        }
        @Override
        public boolean isRecursive() {
            return false;
        }
    }
    
    /**
     * 断言必填参数
     * 
     * @param param
     *            参数
     * @param clazz
     *            类
     * @param method
     *            方法
     * @param parameter
     *            参数名
     */
    public static void assertRequireParameter(Object param, Class<?> clazz, String method, String parameter) {
        Assert.notNull(param,
                        Log.op("require_parameter").msg("parameter is null")
                                .kv("className", DirMonitorServiceUtils.class.getName()).kv("method", method)
                                .kv("parameter", parameter).toString());
    }
}
