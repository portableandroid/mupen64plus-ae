package paulscode.android.mupen64plus;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import java.util.ListIterator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

// TODO: Comment thoroughly
public class MenuSettingsCoreChangeActivity extends ListActivity implements IOptionChooser
{
    public static MenuSettingsCoreChangeActivity mInstance = null;
    private OptionArrayAdapter optionArrayAdapter;  // Array of menu options

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        mInstance = this;

        Globals.checkLocale( this );

        List<MenuOption>optionList = new ArrayList<MenuOption>();
        optionList.add( new MenuOption( getString( R.string.core_import ), getString( R.string.core_new_plgin ), "MenuSettingsCoreChangeImport" ) );

        Config core_list = new Config( Globals.DataDir + "/plug-ins/core_list.ini" );
        ListIterator<Config.ConfigSection> iter = core_list.listIterator();
        Config.ConfigSection section;
        while( iter.hasNext() )
        {   // Loop through the sections
            section = iter.next();
            if( !section.name.equals( "[<sectionless!>]" ) )
                optionList.add( new MenuOption( section.get( "name" ), section.get( "info" ), section.name ) );
        }
        optionArrayAdapter = new OptionArrayAdapter( this, R.layout.menu_option, optionList );
        setListAdapter( optionArrayAdapter );
    }

    public void optionChosen( String option )
    {
        if( option == null )
        {
            Log.e( "MenuSettingsCoreChangeActivity", "option null in method optionChosen" );
            return;
        }
        File archive = new File( option );
        String pluginName = archive.getName();
        if( pluginName == null )
        {
            Log.e( "MenuSettingsCoreChangeActivity", "plug-in name null in method optionChosen" );
            return;
        }
        pluginName = pluginName.substring( 0, pluginName.length() - 3 );
        if( pluginName.length() > 3 )
           pluginName = pluginName.substring( 3, pluginName.length() );

        String targetPath = Globals.LibsDir + "/" + archive.getName();

        File target = new File( targetPath );
        boolean success = Updater.copyFile( archive, target );
        if( success )
        {
            Config cfg = new Config( Globals.DataDir + "/plug-ins/core_list.ini" );
            cfg.put(  targetPath, "name", pluginName );
            cfg.put(  targetPath, "info", "(no description)" );
            cfg.put(  targetPath, "author", "(unknown)" );  // TODO: Dialog to have users enter these if they wish
            cfg.save();
            optionArrayAdapter.add( new MenuOption( pluginName, "(no description)", targetPath ) );
        }
        else
        {
            Log.e( "MenuSettingsCoreChangeActivity", "Error copying file from '" + option + "' to '" + targetPath + "'" );
        }
    }

    /**
     * Determines what to do, based on what option the user chose 
     * @param listView Used by Android.
     * @param view Used by Android.
     * @param position Which item the user chose.
     * @param id Used by Android.
     */
    @Override
    protected void onListItemClick( ListView listView, View view, int position, long id )
    {
        super.onListItemClick( listView, view, position, id );
        MenuOption menuOption = optionArrayAdapter.getOption( position );
        if( menuOption.info.equals( "MenuSettingsCoreChangeImport" ) )
        {  // Open the file menu to choose a plug-in
            String path = MenuActivity.gui_cfg.get( "LAST_SESSION", "so_folder" );

            if( path == null || path.length() < 1 )
                FileChooserActivity.startPath = Globals.StorageDir;
            else
                FileChooserActivity.startPath = path;
            FileChooserActivity.extensions = ".so";
            FileChooserActivity.parentMenu = mInstance;
            FileChooserActivity.function = FileChooserActivity.FUNCTION_SO;
            Intent intent = new Intent( mInstance, FileChooserActivity.class );
            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
            startActivity( intent );
        }
        else
        {
            if(  MenuSettingsCoreActivity.mInstance != null )
                 MenuSettingsCoreActivity.mInstance.optionChosen( menuOption.info );
            mInstance.finish();
        }
    }
}
