package im.in.github;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private TextView titleTv;
	private Handler  typeHandler = new Handler();
	
	private LinearLayout opLayout, androidLayout, webLayout;
	private int screenWidth;

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
        setContentView(R.layout.activity_main);
		titleTv = (TextView) findViewById(R.id.title);
		titleTv.setText("");       
		final int time = 100;
		startTypeWriter("Welcome To TRav", time+20, true, new Runnable() {
				@Override
				public void run() {
					startTypeWriter("Code By ChenQingMua", time, true, new Runnable() {
							@Override
							public void run() {
								startTypeWriter("Hello User", time-20, false, null); 
							}
						});
				}
			});
		
		
		screenWidth = getResources().getDisplayMetrics().widthPixels;

		
		androidLayout = (LinearLayout) findViewById(R.id.android);
		webLayout     = (LinearLayout) findViewById(R.id.web);
		setRippleForeground(androidLayout);
		setRippleForeground(webLayout);
		androidLayout.setTranslationX(-screenWidth);
		webLayout.setTranslationX(-screenWidth);

					androidLayout.animate()
                        .translationX(0)
                        .setDuration(MainActivity.szxcsj)
                        .withEndAction(new Runnable() {
                            public void run() {
                                webLayout.animate()
									.translationX(0)
									.setDuration(MainActivity.szxcsj)
									.start();
                            }
                        })
                        .start();
				
		
        findViewById(R.id.android).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) { showPop(v, R.menu.android); }
			});
        findViewById(R.id.web).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) { showPop(v, R.menu.web); }
			});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            View anchor = findViewById(R.id.action_settings);
            PopupMenu popup = new PopupMenu(this, anchor);
            popup.getMenuInflater().inflate(R.menu.menu, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem menuItem) {
						int id = menuItem.getItemId();
						if (id == R.id.pop_restart) {
							restartApp(); return true;
						} else if (id == R.id.pop_exit) {
							exitApp(); return true;
						}
						return false;
					}
				});
            popup.show();
            return true;
        }
        if (item.getItemId() == R.id.action_about) {
            AlertDialog d = new AlertDialog.Builder(this)
				.setTitle("关于应用")
				.setMessage("一个跑在安卓平台上，开源简单的安卓和网页操作工具！\n\n由“吐司”和“陈青陌”编写，点击下方“源码”按钮跳转至“GitHub”查看源码。\n\n我们也包含其他开源项目，你可以在该项目的“GitHub”主页中的“README.md”里面查看。\n\n允许任何人修改源码，并重新打包，但是严格禁止用于任何付费项目！\n\n为什么我们选择开源?我们只是一个刚刚创立的团队，可能随时跑路，但是为了让圈子里面的人，要学习的使用到我们的项目，所有我们现选择了开发源码。\n\n官方QQ交流群：981546525，感谢你的支持！")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.setNegativeButton("源码", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ChenQingMua/TRav")));
					}
				})
				.create();
            d.setCanceledOnTouchOutside(false);
            d.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPop(View anchor, int menuRes) {
        PopupMenu pop = new PopupMenu(this, anchor);
        pop.getMenuInflater().inflate(menuRes, pop.getMenu());
        pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					return startActById(item.getItemId());
				}
			});
        pop.show();
    }

    private boolean startActById(int id) {
        Intent i = null;
        switch (id) {
            case R.id.android_1: i = new Intent(this, DamagePackage.class); break;
            case R.id.android_2: i = new Intent(this, iAppDecompile.class); break;
            case R.id.android_3: i = new Intent(this, ReinforceCamouflag.class); break;
            case R.id.web_1:     i = new Intent(this, HTMLDecompile.class); break;
            case R.id.web_2:     i = new Intent(this, HTMLEncrypt.class); break;
            case R.id.web_3:     i = new Intent(this, JSEncrypt.class); break;
        }
        if (i != null) { startActivity(i); return true; }
        return false;
    }

    private void restartApp() {
        finish();
        startActivity(getIntent());
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void exitApp() {
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
	private void startTypeWriter(final String text,
								 final long perCharDelay,
								 final boolean clearWhenDone,   
								 final Runnable onDone) {
		titleTv.setText("");               
		final long waitBeforeDone = clearWhenDone ? 500L : 500L; 
		final int len = text.length();

		for (int i = 0; i <= len; i++) {
			final int idx = i;
			typeHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (idx == len) {     
							typeHandler.postDelayed(new Runnable() {
									@Override
									public void run() {
										if (clearWhenDone) titleTv.setText(""); 
										if (onDone != null) onDone.run();
									}
								}, waitBeforeDone);
						} else {
							titleTv.setText(text.substring(0, idx + 1));
						}
					}
				}, perCharDelay * i);
		}
	}
	
	
	@SuppressLint("NewApi")   
	private void setRippleForeground(View v) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			int[][] states = new int[][]{ new int[]{} };
			int[] colors   = new int[]{ 0x1f000000 };   
			ColorStateList colorStateList = new ColorStateList(states, colors);
			RippleDrawable ripple = new RippleDrawable(colorStateList, null, null);
			v.setForeground(ripple);
		}
	}
	
	static final int szxcsj = 400;
	
}

