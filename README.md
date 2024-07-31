随着工信部对APP的一系列整治，现在要求APP在申请相机、位置等敏感权限时需要向用户同步告知权限申请的使用目的。个大应用市场在APP上线审核中也会重点检查这项要求。

从Android 6.0开始，对于敏感权限的获取，APP需要调用系统提供的动态申请权限API， 默认下，系统只是简单弹出一个授权询问框，我们要展示使用说明就要自己实现了。

下面是效果图（中间部分就是系统的授权询问框，顶上的是权限使用目的叠加层）：

![file2](https://github.com/user-attachments/assets/d0d75330-c7ad-4af8-b190-a16b829af2c1)


### 在根项目settings.gradle依赖项解析管理里添加JitPack仓库
```gradle
dependencyResolutionManagement {
	repositories {
		maven { url 'https://jitpack.io' }
	}
}
```

### 在项目build.gradle添加okpermission依赖项
```gradle
dependencies {
	implementation 'com.github.cncomer:okpermission:1.0.0'
}
```

### 动态申请权限
```java
new PermissionLauncher()
        .with(this)  //this可以是androidx.fragment.app.Fragment或者androidx.fragment.app.FragmentActivity
        .granted(() -> {
            //所有权限都授权后回调此处
        })
        .denied((rejectPermissionList) -> {
            //有权限被拒绝回调此处
            //rejectPermissionList为被拒绝的权限列表
        })
        .request(权限, 显示的权限名称, 显示的权限使用目的);
```
<img width="428" alt="AXPE Z TE" src="https://github.com/user-attachments/assets/73f1c82c-837b-4fea-a660-87b2c0ea9a9a">

可以一次请求多个权限，request的时候传入权限数组即可，比如

`.request(new String[]{权限1,权限2}, new String[]{权限1名称,权限2名称}, new String[]{权限1使用目的,权限2使用目的});`

也可以请求多个权限，但只显示一个总的权限名称和使用目的，比如蓝牙权限，在Android 10以前还需要同时申请位置权限才能正常使用，所以我们可以把这两个权限的使用目的合在一起显示。
```java
.request(new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION},
         new String[]{"蓝牙、位置信息授权提示"},
         new String[]{"为了实现扫描、连接蓝牙设备，并与其相互传输数据，需要访问蓝牙相关权限。然而对于Android 10这些相对偏旧的系统，还必须访问位置相关权限。您如果拒绝开启，将无法使用上述功能。"});
```

<img width="471" alt="E7  DERSENI" src="https://github.com/user-attachments/assets/14548c1d-50f6-4dca-8697-f504a602bbdc">

不需要显示权限目的说明，直接请求权限即可。
```java
.request(权限);
//或者
.request(权限数组);
```
